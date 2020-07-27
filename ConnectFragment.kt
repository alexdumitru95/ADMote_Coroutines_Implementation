package dumyxd.ADMote


import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.ads.consent.ConsentInformation
import com.google.ads.consent.ConsentStatus
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import dumyxd.ADMote.MainActivity.*
import kotlinx.android.synthetic.main.fragment_connect.*
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.Socket
import java.security.*
import java.security.cert.CertificateException
import java.util.*
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.TrustManagerFactory


class ConnectFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var connectButton: Button
    private lateinit var disconnectButton: Button
    private lateinit var IPAddress: AppCompatEditText
    private lateinit var PortTCP: AppCompatEditText
    private var mContext: Context? = null
    private lateinit var ipAddressNA: TextView
    private lateinit var portNumberNA: TextView
    private lateinit var statusConexiune: TextView
    private lateinit var lastConnectionButton: RelativeLayout
    private lateinit var wifiStatus: ImageView
    private lateinit var adView: AdView
    private lateinit var mSharedPreferences: SharedPreferences
    private val TAG = this.javaClass.simpleName

    private lateinit var setupConnection: SetupConnection
    private lateinit var setupConnectionLastDetails: SetupConnectionLastDetails


    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        mSharedPreferences = mContext!!.getSharedPreferences(mContext!!.packageName, Context.MODE_PRIVATE)
        sharedPreferences = mContext!!.getSharedPreferences("lastConnectionDetails", Context.MODE_PRIVATE)
        Log.d(TAG, "onAtach")
    }

    override fun onDetach() {
        super.onDetach()
        mContext = null
        Log.d(TAG, "onDetach")
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view: View = inflater.inflate(R.layout.fragment_connect, container, false)
        setHasOptionsMenu(true)
        retainInstance = true
        val toolbar: Toolbar? = activity?.findViewById(R.id.nav_action)
        toolbar?.setTitle(R.string.admote_control_title)
        toolbar?.setBackgroundColor(Color.parseColor("#161616"))
        adView = view.findViewById(R.id.adView)

        // Google AdMob Consent preferences
        val consentStatus = ConsentInformation.getInstance(activity).consentStatus
        if (consentStatus.toString() == "NON_PERSONALIZED") {
            showNonPersonalizedAds()
        } else {
            showPersonalizedAds()
        }

        // View elements
        IPAddress = view.findViewById(R.id.IPAddress)
        PortTCP = view.findViewById(R.id.PortTCP)
        connectButton = view.findViewById(R.id.connectButton)
        disconnectButton = view.findViewById(R.id.disconnectButton)
        disconnectButton.isEnabled = false
        lastConnectionButton = view.findViewById(R.id.lastConnectionButton)
        ipAddressNA = view.findViewById(R.id.ipNotAvailable)
        portNumberNA = view.findViewById(R.id.portNotAvailable)
        connectionStatus = view.findViewById(R.id.connectionStatus)
        wifiStatus = view.findViewById(R.id.wifidisonnected)


        val officialwebsite = view.findViewById<TextView>(R.id.officialwebsite)
        val websitehelp = view.findViewById<TextView>(R.id.websitehelp)

        val lastConnectionDetails = getlastConnectionDetails()
        ipAddressNA.text = lastConnectionDetails[0]
        portNumberNA.text = lastConnectionDetails[1]


        // Click listener for displaying official website
        officialwebsite.setOnClickListener {
            if (resources.configuration.locale.language == "ro") {
                val uri = Uri.parse("https://admotecontrol.com/ro")
                val goToWebsite = Intent(Intent.ACTION_VIEW, uri)
                goToWebsite.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                try {
                    startActivity(goToWebsite)
                } catch (e: ActivityNotFoundException) {
                    startActivity(Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://admotecontrol.com/ro")))
                }
            } else if (resources.configuration.locale.language == "ru") {
                val uri = Uri.parse("https://admotecontrol.com/ru")
                val goToWebsite = Intent(Intent.ACTION_VIEW, uri)
                goToWebsite.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                try {
                    startActivity(goToWebsite)
                } catch (e: ActivityNotFoundException) {
                    startActivity(Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://admotecontrol.com/ru")))
                }
            } else {
                val uri = Uri.parse("https://admotecontrol.com/")
                val goToWebsite = Intent(Intent.ACTION_VIEW, uri)
                goToWebsite.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                try {
                    startActivity(goToWebsite)
                } catch (e: ActivityNotFoundException) {
                    startActivity(Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://admotecontrol.com/")))
                }
            }
        }

         // Click listener for displaying help website
        websitehelp.setOnClickListener {
            if (resources.configuration.locale.language == "ro") {
                val uri = Uri.parse("https://admotecontrol.com/ro/ajutor")
                val goToWebsite = Intent(Intent.ACTION_VIEW, uri)
                goToWebsite.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                try {
                    startActivity(goToWebsite)
                } catch (e: ActivityNotFoundException) {
                    startActivity(Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://admotecontrol.com/ro/ajutor")))
                }
            } else if (resources.configuration.locale.language == "ru") {
                val uri = Uri.parse("https://admotecontrol.com/ru/pomoshch")
                val goToWebsite = Intent(Intent.ACTION_VIEW, uri)
                goToWebsite.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                try {
                    startActivity(goToWebsite)
                } catch (e: ActivityNotFoundException) {
                    startActivity(Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://admotecontrol.com/ru/pomoshch")))
                }
            } else {
                val uri = Uri.parse("https://admotecontrol.com/help")
                val goToWebsite = Intent(Intent.ACTION_VIEW, uri)
                goToWebsite.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                try {
                    startActivity(goToWebsite)
                } catch (e: ActivityNotFoundException) {
                    startActivity(Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://admotecontrol.com/help")))
                }
            }
        }


        // Settings some rules for the view to update when connection is/is not available
        if (clientSocket != null) {
            connectButton.setText(R.string.conectat_cu_succes)
            connectButton.isEnabled = false
            connectButton.setTextColor(Color.parseColor("#FFFFFF"))
            disconnectButton.isEnabled = true
            disconnectButton.setTextColor(Color.parseColor("#FFFFFF"))
            IPAddress.isEnabled = false
            PortTCP.isEnabled = false
            lastConnectionButton.isEnabled = false
            wifiStatus.setImageDrawable(ContextCompat.getDrawable(activity as MainActivity, R.drawable.wificonnected))
            statusConexiune.setText(R.string.conexiune_activa)
            statusConexiune.setTextColor(Color.parseColor("#FFFFFF"))
        } else {
            connectButton.setText(R.string.conectare)
            connectButton.setTextColor(Color.parseColor("#FFFFFF"))
            connectButton.isEnabled = true
            disconnectButton.isEnabled = false
            disconnectButton.setTextColor(Color.parseColor("#FFFFFF"))
            lastConnectionButton.isEnabled = true
            statusConexiune.setText(R.string.deconectat)
            statusConexiune.setTextColor(Color.parseColor("#FFFFFF"))
            wifiStatus.setImageDrawable(ContextCompat.getDrawable(activity as MainActivity, R.drawable.wifidisconnected))
            IPAddress.isEnabled = true
            PortTCP.isEnabled = true
        }

        // connectButton onClickListener
        connectButton.setOnClickListener {
            connectionConfirmation()
        }

        // disconnectButton onClickListenr
        disconnectButton.setOnClickListener {
            try {
                objectInputStream.close()
                objectOutputStream.close()
                clientSocket.close()
                clientSocket = null
                resetConnection()
                connectButton.setText(R.string.conectare)
                connectButton.setTextColor(Color.parseColor("#FFFFFF"))
                connectButton.isEnabled = true
                disconnectButton.isEnabled = false
                disconnectButton.setTextColor(Color.parseColor("#FFFFFF"))
                lastConnectionButton.isEnabled = true
                statusConexiune.setText(R.string.deconectat)
                statusConexiune.setTextColor(Color.parseColor("#FFFFFF"))
                wifiStatus.setImageDrawable(ContextCompat.getDrawable(activity as MainActivity, R.drawable.wifidisconnected))
                IPAddress.isEnabled = true
                PortTCP.isEnabled = true
            } catch (ignored: Exception) {

            }
        }

        // lastConnectionButton onClickListener
        lastConnectionButton.setOnClickListener {
            connectionLastDetailsConfirmation()
        }
        return view
    }



    // inner class used for setting up the connection
    inner class SetupConnection : CreateConnection<String, Void, Socket?>() {

        override fun doInBackground(vararg params: String?): Socket? {

            // SSL certs initialization
            try {
                val trustSt = KeyStore.getInstance("BKS")
                val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
                val trustStoreStream = mResources.openRawResource(R.raw.admotecontrol)
                trustSt.load(trustStoreStream, "alexkarisma95".toCharArray())
                trustManagerFactory.init(trustSt)
                val keyStore = KeyStore.getInstance("BKS")
                val keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
                val keyStoreStream = mResources.openRawResource(R.raw.admotecontrol)
                keyStore.load(keyStoreStream, "alexkarisma95".toCharArray())
                keyManagerFactory.init(keyStore, "alexkarisma95".toCharArray())
                val sslctx = SSLContext.getInstance("TLS")
                sslctx.init(null, trustManagerFactory.trustManagers, SecureRandom())


                // Create a SSLSocketFactory that allows for self signed certs
                val sslSocketFactory = sslctx.socketFactory
                clientSocket = sslSocketFactory.createSocket(Socket(IPAddress.text.toString(), PortTCP.text.toString().toInt()), IPAddress.text.toString(), PortTCP.text.toString().toInt(), false) as SSLSocket?
                (clientSocket as SSLSocket?)?.startHandshake()
                val startTime = System.currentTimeMillis()
                objectInputStream = ObjectInputStream(clientSocket!!.getInputStream())
                objectOutputStream = ObjectOutputStream(clientSocket!!.getOutputStream())
                val time = System.currentTimeMillis() - startTime
                val tag = "TCPClient"
                Log.d(tag, "Successfully connected! The connection was established in : " + time + "ms")
            } catch (e: KeyManagementException) {
                Log.e("ADMote", "The connection with the server failed!", e)
                clientSocket = null
            } catch (e: IOException) {
                Log.e("ADMote", "The connection with the server failed!", e)
                clientSocket = null
            } catch (e: KeyStoreException) {
                Log.e("ADMote", "The connection with the server failed!", e)
                clientSocket = null
            } catch (e: CertificateException) {
                Log.e("ADMote", "The connection with the server failed!", e)
                clientSocket = null
            } catch (e: NoSuchAlgorithmException) {
                Log.e("ADMote", "The connection with the server failed!", e)
                clientSocket = null
            } catch (e: UnrecoverableKeyException) {
                Log.e("ADMote", "The connection with the server failed!", e)
                clientSocket = null
            }

            return clientSocket
        }

        override fun receiveData(result: Socket?) {
            clientSocket = result as SSLSocket?
            if (clientSocket == null) {
                errorMessage()
                connectButton.setText(R.string.conectare)
                connectButton.setTextColor(Color.parseColor("#FFFFFF"))
                connectButton.isEnabled = true
                disconnectButton.isEnabled = false
                lastConnectionButton.isEnabled = true
                statusConexiune.setText(R.string.conexiune_indisponibila)
                IPAddress.isEnabled = true
                PortTCP.isEnabled = true
            } else {
                successfullyConnected()
                connectButton.setText(R.string.conectat_cu_succes)
                connectButton.setTextColor(Color.parseColor("#FFFFFF"))
                disconnectButton.isEnabled = true
                disconnectButton.setTextColor(Color.parseColor("#FFFFFF"))
                statusConexiune.setText(R.string.conexiune_activa)
                statusConexiune.setTextColor(Color.parseColor("#FFFFFF"))
                wifiStatus.setImageDrawable(ContextCompat.getDrawable(activity as MainActivity, R.drawable.wificonnected))
                IPAddress.isEnabled = false
                PortTCP.isEnabled = false
            }

        }

    }

    // inner class used for setting up the connection by using the last connection details
    inner class SetupConnectionLastDetails : CreateConnectionLastDetails<String, Void, Socket?>() {

        override fun doInBackground(vararg params: String?): Socket? {

            // SSL certs initialization
            try {
                val trustSt = KeyStore.getInstance("BKS")
                val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
                val trustStoreStream = mResources.openRawResource(R.raw.admotecontrol)
                trustSt.load(trustStoreStream, "alexkarisma95".toCharArray())
                trustManagerFactory.init(trustSt)
                val keyStore = KeyStore.getInstance("BKS")
                val keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
                val keyStoreStream = mResources.openRawResource(R.raw.admotecontrol)
                keyStore.load(keyStoreStream, "alexkarisma95".toCharArray())
                keyManagerFactory.init(keyStore, "alexkarisma95".toCharArray())
                val sslctx = SSLContext.getInstance("TLS")
                sslctx.init(null, trustManagerFactory.trustManagers, SecureRandom())


                // Create a SSLSocketFactory that allows for self signed certs
                val sslSocketFactory = sslctx.socketFactory
                clientSocket = sslSocketFactory.createSocket(Socket(ipNotAvailable.text.toString(), portNotAvailable.text.toString().toInt()), ipNotAvailable.text.toString(), portNotAvailable.text.toString().toInt(), false) as SSLSocket?
                (clientSocket as SSLSocket?)?.startHandshake()
                val startTime = System.currentTimeMillis()
                objectInputStream = ObjectInputStream(clientSocket!!.getInputStream())
                objectOutputStream = ObjectOutputStream(clientSocket!!.getOutputStream())
                val time = System.currentTimeMillis() - startTime
                val tag = "TCPClient"
                Log.d(tag, "Successfully connected! The connection was established in : " + time + "ms")
            }
            catch (e: KeyManagementException) {
                Log.e("ADMote", "The connection with the server failed!", e)
                clientSocket = null
            }
            catch(e: IOException) {
                Log.e("ADMote", "The connection with the server failed!", e)
                clientSocket = null
            }
            catch(e: KeyStoreException) {
                Log.e("ADMote", "The connection with the server failed!", e)
                clientSocket = null
            }
            catch(e: CertificateException) {
                Log.e("ADMote", "The connection with the server failed!", e)
                clientSocket = null
            }
            catch(e: NoSuchAlgorithmException) {
                Log.e("ADMote", "The connection with the server failed!", e)
                clientSocket = null
            }
            catch(e: UnrecoverableKeyException) {
                Log.e("ADMote", "The connection with the server failed!", e)
                clientSocket = null
            }

            return clientSocket
        }

        override fun receiveData(result: Socket?) {
            clientSocket = result as SSLSocket?
            if (clientSocket == null) {
                errorMessage()
                connectButton.setText(R.string.conectare)
                connectButton.setTextColor(Color.parseColor("#FFFFFF"))
                connectButton.isEnabled = true
                disconnectButton.isEnabled = false
                lastConnectionButton.isEnabled = true
                statusConexiune.setText(R.string.conexiune_indisponibila)
                wifiStatus.setImageDrawable(ContextCompat.getDrawable(activity as MainActivity, R.drawable.questionmark))
                IPAddress.isEnabled = true
                PortTCP.isEnabled = true
            } else {
                successfullyConnected()
                wifiStatus.setImageDrawable(ContextCompat.getDrawable(activity as MainActivity, R.drawable.wificonnected))
                connectButton.setText(R.string.conectat_cu_succes)
                connectButton.setTextColor(Color.parseColor("#FFFFFF"))
                disconnectButton.isEnabled = true
                disconnectButton.setTextColor(Color.parseColor("#FFFFFF"))
                statusConexiune.setText(R.string.conexiune_activa)
                statusConexiune.setTextColor(Color.parseColor("#FFFFFF"))
                ipAddressNA.isEnabled = false
                portNumberNA.isEnabled = false
            }

        }

    }

    // 
    private fun getLastConnectionDetails(): Array<String?> {
        val arr = arrayOfNulls<String>(2)
        arr[0] = sharedPreferences.getString("ultimulIPConectat", "N/A")
        arr[1] = sharedPreferences.getString("ultimulPortConectat", "N/A")
        return arr
    }

    private fun setLastConnectionDetails(arr: Array<String>) {
        val editor = sharedPreferences.edit()
        editor.putString("ultimulIPConectat", arr[0])
        editor.putString("ultimulPortConectat", arr[1])
        editor.apply()
    }


    // Google AdMob Consent - Show personalized ads
    private fun showPersonalizedAds() {
        if (mSharedPreferences.getBoolean(getString(R.string.ad_free_purchase), true)) {
            ConsentInformation.getInstance(activity).consentStatus = ConsentStatus.PERSONALIZED
            val adRequest = AdRequest.Builder().build()
            adView.loadAd(adRequest)
            Log.d(TAG, "Personalized")
        } else {
            adView.visibility = View.GONE
        }
    }

    // Google AdMob Consent - Show non-personalized ads
    private fun showNonPersonalizedAds() {
        if (mSharedPreferences.getBoolean(getString(R.string.ad_free_purchase), true)) {
            ConsentInformation.getInstance(activity).consentStatus = ConsentStatus.NON_PERSONALIZED
            val adRequest = AdRequest.Builder()
                    .addNetworkExtrasBundle(AdMobAdapter::class.java, nonPersonalizedAdsBundle)
                    .build()
            adView.loadAd(adRequest)
            Log.d(TAG, "Non Personalized")
        } else {
            adView.visibility = View.GONE
        }
    }

    private val nonPersonalizedAdsBundle: Bundle
        get() {
            val extras = Bundle()
            extras.putString("npa", "1")
            return extras
        }


    // Dialog for informing member that in order to connect, the server needs to be opened on the PC/laptop
    private fun connectionConfirmationDialog(c: Context): AlertDialog.Builder? {

        val builder = AlertDialog.Builder(c, R.style.DialogTheme)
        builder.setCancelable(false)
        builder.setMessage("""
    ${getString(R.string.connection_confirmation)}
    
    ${getString(R.string.connection_confirmation2)}
    
    ${getString(R.string.connection_confirmation3)}
    
    ${getString(R.string.connection_confirmation4)}
    """.trimIndent())
        builder.setPositiveButton(
                getString(R.string.connect_confirmation)
        ) { dialog, id -> startConnection()

        }
        builder.setNegativeButton(
                getString(R.string.go_back_confirmation)
        ) { dialog, id -> dialog.dismiss()

        }
        return builder
    }

    private fun connectionConfirmation() {
        connectionConfirmationDialog(mContext!!)!!.show()
    }

    // --------End of confirmation dialog----------------------------


    // Dialog for informing member that in order to connect, the server needs to be opened on the PC/laptop - Last connection details
    private fun connectionLastDetailsConfirmationDialog(c: Context): AlertDialog.Builder? {

        val builder = AlertDialog.Builder(c, R.style.DialogTheme)
        builder.setCancelable(false)
        builder.setMessage("""
    ${getString(R.string.connection_confirmation)}
    
    ${getString(R.string.connection_confirmation2)}
    
    ${getString(R.string.connection_confirmation3)}
    
    ${getString(R.string.connection_confirmation4)}
    """.trimIndent())
        builder.setPositiveButton(
                getString(R.string.connect_confirmation)
        ) { dialog, id -> startConnectionLastDetails()

        }
        builder.setNegativeButton(
                getString(R.string.go_back_confirmation)
        ) { dialog, id -> dialog.dismiss()

        }
        return builder
    }

    private fun connectionLastDetailsConfirmation() {
        connectionLastDetailsConfirmationDialog(mContext!!)!!.show()
    }
    // --------End of confirmation dialog----------------------------


    // Dialog for displaying the error message and possible cause of a connection error
    private fun errorMessageDialog(c: Context): AlertDialog.Builder? {

        val builder = AlertDialog.Builder(c, R.style.DialogTheme)
        builder.setTitle(R.string.connection_error)
        builder.setCancelable(false)
        builder.setMessage("""
            
    ${getString(R.string.connection_error_reason6)}  
            
    ${getString(R.string.connection_error_reason1)}
    
    ${getString(R.string.connection_error_reason2)}
    
    ${getString(R.string.connection_error_reason3)}
    
    ${getString(R.string.connection_error_reason4)}
    
    ${getString(R.string.connection_error_reason5)}
    """.trimIndent())
        builder.setPositiveButton(
                getString(R.string.visit_help_website)
        ) { dialog, id -> visitHelpWebsite()

        }
        builder.setNegativeButton(
                getString(R.string.go_back_confirmation)
        ) { dialog, id -> dialog.dismiss()

        }

        return builder
    }

    private fun errorMessage() {
        errorMessageDialog(mContext!!)!!.show()
    }

// --------End of error dialog----------------------------



    // Dialog that confirms that the connection was successfully established
    private fun successfullyConnectedDialog(c: Context): AlertDialog.Builder? {

        val builder = AlertDialog.Builder(c, R.style.DialogTheme)
        builder.setTitle(R.string.connection_success_title)
        builder.setCancelable(false)
        builder.setMessage("""
            
    ${getString(R.string.connection_success1)}  
            
    ${getString(R.string.connection_success2)}
    """.trimIndent())
        builder.setPositiveButton(
                getString(R.string.ok_got_it)
        ) { dialog, id -> dialog.dismiss()

        }
        
        return builder
    }

    private fun successfullyConnected() {
        successfullyConnectedDialog(mContext!!)!!.show()
    }

// --------End of success connection dialog----------------------------


    // Dialog that confirms that the connection was successfully reset
    private fun resetConnectionDialog(c: Context): AlertDialog.Builder? {

        val builder = AlertDialog.Builder(c, R.style.DialogTheme)
        builder.setTitle(R.string.reset_connection_title)
        builder.setCancelable(false)
        builder.setMessage("""
            
    ${getString(R.string.reset_connection1)}  
            
    ${getString(R.string.reset_connection2)}

    """.trimIndent())
        builder.setPositiveButton(
                getString(R.string.ok_got_it)
        ) { dialog, id -> dialog.dismiss()

        }

        return builder
    }

    private fun resetConnection() {
        resetConnectionDialog(mContext!!)!!.show()
    }
// --------End of reset connection dialog----------------------------


    // Start normal and last details connection
    private fun startConnection() {
        setupConnection = SetupConnection()
        if (ValidateIP.validateIP(IPAddress.text.toString()) && ValidateIP.validatePort(PortTCP.text.toString())) {
            setUltimeleDetaliiConexiune(arrayOf(IPAddress.text.toString(), PortTCP.text.toString()))
            connectButton.setText(R.string.conectare_in_derulare)
            connectButton.setTextColor(Color.parseColor("#FFFFFF"))
            connectButton.isEnabled = false
            ipAddressNA.text = IPAddress.text.toString()
            portNumberNA.text = PortTCP.text.toString()
            lastConnectionButton.isEnabled = false
            statusConexiune.setText(R.string.conectare_in_derulare)
            statusConexiune.setTextColor(Color.parseColor("#FFFFFF"))
            wifiStatus.setImageDrawable(ContextCompat.getDrawable(activity as MainActivity, R.drawable.questionmark))
            IPAddress.isEnabled = false
            PortTCP.isEnabled = false
            setupConnection.execute()
        }
        else {
            Toast.makeText(activity, R.string.invalid_ip_port, Toast.LENGTH_SHORT).show()
        }
    }


    private fun startConnectionLastDetails() {

        setupConnectionLastDetails = SetupConnectionLastDetails()
        if (ValidateIP.validateIP(ipNotAvailable.text.toString()) && ValidateIP.validatePort(portNotAvailable.text.toString())) {
            setUltimeleDetaliiConexiune(arrayOf(ipNotAvailable.text.toString(), portNotAvailable.text.toString()))
            connectButton.isEnabled = false
            lastConnectionButton.isEnabled = false
            statusConexiune.setText(R.string.conectare_in_derulare)
            statusConexiune.setTextColor(Color.parseColor("#FFFFFF"))
            wifiStatus.setImageDrawable(ContextCompat.getDrawable(activity as MainActivity, R.drawable.questionmark))
            IPAddress.isEnabled = false
            PortTCP.isEnabled = false
            setupConnectionLastDetails.execute()
        }
        else {
            Toast.makeText(activity, R.string.invalid_ip_port, Toast.LENGTH_SHORT).show()
        }
    }

    // ---------------------------------------------------------------------

    
    private fun visitHelpWebsite() {
        if (resources.configuration.locale.language == "ro") {
            val uri = Uri.parse("https://admotecontrol.com/ro/ajutor")
            val goToWebsite = Intent(Intent.ACTION_VIEW, uri)
            goToWebsite.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            try {
                startActivity(goToWebsite)
            } catch (e: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://admotecontrol.com/ro/ajutor")))
            }
        } else if (resources.configuration.locale.language == "ru") {
            val uri = Uri.parse("https://admotecontrol.com/ru/pomoshch")
            val goToWebsite = Intent(Intent.ACTION_VIEW, uri)
            goToWebsite.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            try {
                startActivity(goToWebsite)
            } catch (e: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://admotecontrol.com/ru/pomoshch")))
            }
        } else {
            val uri = Uri.parse("https://admotecontrol.com/help")
            val goToWebsite = Intent(Intent.ACTION_VIEW, uri)
            goToWebsite.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            try {
                startActivity(goToWebsite)
            } catch (e: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://admotecontrol.com/help")))
            }
        }
    }


}




