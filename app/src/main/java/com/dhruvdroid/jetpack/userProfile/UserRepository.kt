package com.dhruvdroid.jetpack.userProfile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository modules handle data operations. They provide a clean API so that the rest of the app
 * can retrieve this data easily. They know where to get the data from and what API calls to make
 * when data is updated. You can consider repositories to be mediators between different data sources,
 * such as persistent models, web services, and caches.
 *
 * Important Purpose: it abstracts the data sources from the rest of the app. Now,
 * our UserProfileViewModel doesn't know how the data is fetched, so we can provide
 * the view model with data obtained from several different data-fetching implementations.
 *
 */

//
// Created by Dhruv Singh on 26/06/20.
//

@Singleton
class UserRepository @Inject constructor(
    private val webservice: Webservice,
    private val executor: Executor,
    private val userDao: UserDao/*,
    private val userCache: UserCache*/
) {

    fun getUser(userId: String): LiveData<User> {
        // TODO add the user obj in the memory cache
        /*val cached: LiveData<User> = userCache?.load(userId)
        if (cached != null) {
            return cached
        }*/

        val data = MutableLiveData<User>()
        // The LiveData object is currently empty, but it's okay to add it to the
        // cache here because it will pick up the correct data once the query
        // completes.

        // TODO add the user obj in the memory cache
        /*userCache.put(userId, data)*/

        // This implementation is still suboptimal but better than before.
        // A complete implementation also handles error cases.
        webservice.getUser(userId).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                data.value = response.body()
            }

            // Error case is left out for brevity.
            override fun onFailure(call: Call<User>, t: Throwable) {
                // TODO()
            }
        })
        return data
    }

    private fun refreshUser(userId: String) {
        // Runs in a background thread.
        executor.execute {
            // Check if user data was fetched recently.
            /*val userExists = userDao.hasUser(FRESH_TIMEOUT)*/
            if (!userExists) {
                // Refreshes the data.
                val response = webservice.getUser(userId).execute()

                // Check for errors here.

                // Updates the database. The LiveData object automatically
                // refreshes, so we don't need to do anything else here.
                userDao.save(response.body()!!)
            }
        }
    }


//    fun loadUser(login: String): LiveData<Resource<User>> {
//        return object : NetworkBoundResource<User, User>(appExecutors) {
//            override fun saveCallResult(item: User) {
//                userDao.insert(item)
//            }
//
//            override fun shouldFetch(data: User?) = data == null
//
//            override fun loadFromDb() = userDao.findByLogin(login)
//
//            override fun createCall() = githubService.getUser(login)
//        }.asLiveData()
//    }

    companion object {
        val FRESH_TIMEOUT = TimeUnit.DAYS.toMillis(1)
    }
}