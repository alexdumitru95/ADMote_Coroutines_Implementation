package dumyxd.ADMote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


abstract class CreateConnectionLastDetails<Params, Progress, Result> {

    abstract fun doInBackground(vararg params: Params?): Result?

    open fun onPostExecute(result: Result?) {

        GlobalScope.launch (Dispatchers.Main) {
            receiveData(result)
        }

    }

    abstract fun receiveData(result: Result?)

    fun execute(vararg params: Params?) {

        GlobalScope.launch(Dispatchers.IO) {
            val result = doInBackground(*params)

            withContext(Dispatchers.IO) {
                onPostExecute(result)
            }


        }
    }



}