package goodsoft.com

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import goodsoft.com.db.DBHelper
import goodsoft.com.extencions.setOnClickListener
import goodsoft.com.models.Account
import goodsoft.com.models.GameStatus
import goodsoft.com.utils.Game
import org.json.JSONArray
import org.json.JSONObject
import java.awt.Dimension
import java.io.IOException
import java.io.InputStream
import java.net.InetSocketAddress
import java.util.*
import java.util.concurrent.Executors
import javax.swing.JButton
import javax.swing.JFrame
import kotlin.collections.HashMap


object Main {
    private var serverUp = false
    private var dbHelper: DBHelper? = null

    private var accounts: HashMap<String, Account>? = HashMap()

    @JvmStatic
    fun main(args: Array<String>) {
        showFrame()

        dbHelper = DBHelper.initDB()

        mock()
    }

    fun showFrame() {
        val port = 5001

        var frame = JFrame("Servant")
        frame.size = Dimension(400, 400)
        frame.isVisible = true
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE;
        val buttonOn = JButton()
        buttonOn.text = "On"
        startServer(port)

        buttonOn.setOnClickListener {
            serverUp = if (!serverUp) {
                startServer(port)
                buttonOn.text = "Off"
                true
            } else {
                buttonOn.text = "On"
                stopServer()
                false
            }

        }
        frame.add(buttonOn)
        frame.revalidate()
    }


    private fun mock() {
        val name = "Jim Smith"
        val name2 = "pidor"
        val account = Account(name, null, false, false, "_secret", 500, 3, null)
        val account_2 = Account(name2, null, false, false, "_secret", 500, 3, null)
        val account_3 = Account("test", null, false, false, "test", 500, 3, null)
        val account_4 = Account("test1", null, false, false, "test1", 500, 3, null)

// persist the account object to the database

// persist the account object to the database
        dbHelper?.accountDao?.createIfNotExists(account)
        dbHelper?.accountDao?.createIfNotExists(account_2)
        dbHelper?.accountDao?.createIfNotExists(account_3)
        dbHelper?.accountDao?.createIfNotExists(account_4)

        val tempAccounts = dbHelper?.accountDao?.queryForAll()
        dbHelper?.close()
        tempAccounts?.forEach {
            accounts?.put(it.name, it)
            println("Аккаунт - " + it.name)
        }
    }


    private fun streamToString(inputStream: InputStream): String {
        val s = Scanner(inputStream).useDelimiter("\\A")
        return if (s.hasNext()) s.next() else ""
    }

    private fun sendResponse(httpExchange: HttpExchange, responseText: String) {
        httpExchange.sendResponseHeaders(200, responseText.length.toLong())
        val os = httpExchange.responseBody
        os.write(responseText.toByteArray())
        os.close()
    }

    private var mHttpServer: HttpServer? = null


    private fun startServer(port: Int) {
        try {
            mHttpServer = HttpServer.create(InetSocketAddress(port), 0)
            mHttpServer!!.executor = Executors.newCachedThreadPool()

            mHttpServer!!.createContext("/", rootHandler)
            mHttpServer!!.createContext("/index", rootHandler)
            // Handle /messages endpoint
            mHttpServer!!.createContext("/auth", loginHandler)
            mHttpServer!!.createContext("/startFight", startFightHandler)
            mHttpServer!!.createContext("/setAction", setActionHandler)
            mHttpServer!!.createContext("/resetKicks", resetKicksHandler)
            mHttpServer!!.createContext("/getStatus", statusFightHandler)
            mHttpServer!!.createContext("/endRaund", endRoundHandler)
            mHttpServer!!.createContext("/endGame", endFightHandler)
            mHttpServer!!.createContext("/fighters", fightersListHandler)
            mHttpServer!!.start()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    private fun stopServer() {
        if (mHttpServer != null) {
            mHttpServer!!.stop(0)
        }
    }

    // Handler for root endpoint
    private val rootHandler = HttpHandler { exchange ->
        run {
            // Get request method
            when (exchange!!.requestMethod) {
                "GET" -> {
                    sendResponse(exchange, "Welcome to my server")
                }

            }
        }
    }

//id id

    private val loginHandler = HttpHandler { httpExchange ->
        run {
            when (httpExchange!!.requestMethod) {
                "POST" -> {
                    val inputStream = httpExchange.requestBody

                    val requestBody = streamToString(inputStream)
                    val jsonBody = JSONObject(requestBody)
                    val login = jsonBody.getString("login")
                    val password = jsonBody.getString("password")

                    System.out.println("SER -" + "login ${login} password ${password}")

                    val json = login(login, password)

                    sendResponse(httpExchange, json.toString())
                }
            }
        }
    }

    private fun login(login: String, password: String): JSONObject {
        val jsonObject = JSONObject()

        if (accounts?.containsKey(login) == true) {
            jsonObject.put("status", "success")
        }

        return jsonObject
    }

    private val startFightHandler = HttpHandler { httpExchange ->
        run {
            when (httpExchange!!.requestMethod) {
                "POST" -> {
                    val inputStream = httpExchange.requestBody

                    val requestBody = streamToString(inputStream)
                    val jsonBody = JSONObject(requestBody)
                    val firstPlayerId = jsonBody.getString("firstPlayerId")
                    val secondPlayerId = jsonBody.getString("secondPlayerId")

                    System.out.println("SER -" + "firstPlayerId ${firstPlayerId} secondPlayerId ${secondPlayerId}")
                    val json = startFight(firstPlayerId, secondPlayerId)
                    // save message to database

                    //for testing
                    sendResponse(httpExchange, json.toString())
                }
            }
        }
    }

    private val setActionHandler = HttpHandler { httpExchange ->
        run {
            when (httpExchange!!.requestMethod) {
                "POST" -> {
                    val inputStream = httpExchange.requestBody

                    val requestBody = streamToString(inputStream)
                    val jsonBody = JSONObject(requestBody)
                    val firstPlayerId = jsonBody.getString("firstPlayerId")
                    val secondPlayerId = jsonBody.getString("secondPlayerId")
                    val kickType = jsonBody.getString("kick_type")

                    System.out.println("SER -" + "player id ${firstPlayerId} kick type  ${kickType}")
                    val json = setAction(firstPlayerId, secondPlayerId, Account.KickType.valueOf(kickType.toUpperCase()))
                    // save message to database
//
                    //for testing
                    sendResponse(httpExchange, json.toString())
                }
            }
        }
    }

    private fun setAction(firstPlayerId: String, secondPlayerId: String, kickType: Account.KickType): JSONObject {
        val firstPlayer = accounts?.get(firstPlayerId)
        val secondPlayer = accounts?.get(secondPlayerId)

        firstPlayer?.isEndRound = false
        secondPlayer?.isEndRound = false

        firstPlayer?.kickType = kickType
        firstPlayer?.isMove = true

        if (secondPlayer?.isMove == true) {
            calculateHealAndEnergy(firstPlayer, secondPlayer)
        }

        val status = if (firstPlayer!!.heals!! <= 0)
            GameStatus.LUSE
        else if (secondPlayer!!.heals!! <= 0) GameStatus.WIN
        else GameStatus.FIGHT

        if (status == GameStatus.WIN || status == GameStatus.LUSE) {
            if (status == GameStatus.WIN) {
                secondPlayer?.gameStatus = GameStatus.LUSE
            } else if (status == GameStatus.LUSE) {
                secondPlayer?.gameStatus = GameStatus.WIN
            } else {
                secondPlayer?.gameStatus = GameStatus.FIGHT
            }

            firstPlayer.heals = 500
            secondPlayer?.heals = 500

            firstPlayer.energy = 3
            secondPlayer?.energy = 3
        } else {
            firstPlayer.gameStatus = status
            secondPlayer?.gameStatus = status
        }


        val jsonObject = JSONObject()
        jsonObject.put("firstPlayer", firstPlayer?.toJson())
        jsonObject.put("secondPlayer", secondPlayer?.toJson())
        jsonObject.put("status", status.status)


        accounts?.put(firstPlayer!!.name, firstPlayer)
        accounts?.put(secondPlayer!!.name, secondPlayer)

        return jsonObject
    }

    private fun calculateHealAndEnergy(firstPlayer: Account?, secondPlayer: Account?) {
        if (firstPlayer == null || secondPlayer == null) return

        if (firstPlayer.kickType !== Account.KickType.FINGER_IN_IES) {
            calculateEnergy(secondPlayer)
        }

        if (secondPlayer.kickType !== Account.KickType.FINGER_IN_IES) {
            calculateEnergy(firstPlayer)
        }

        when (firstPlayer?.kickType) {

            Account.KickType.HEND, Account.KickType.LEG, Account.KickType.FINGER_IN_IES -> {
                firstPlayer.kickType?.let { kickType ->
                    when (secondPlayer?.kickType) {
                        Account.KickType.HEND,
                        Account.KickType.LEG,
                        Account.KickType.FINGER_IN_IES,
                        Account.KickType.PAIN_KICK -> {
                            secondPlayer?.heals = secondPlayer?.heals!! - Game.getHealFromKick(kickType)
                        }
                    }
                }
            }
            Account.KickType.KONTR_ATTACK -> {
                firstPlayer.kickType?.let { kickType ->
                    if (secondPlayer.kickType !== Account.KickType.KONTR_ATTACK) secondPlayer?.heals = secondPlayer?.heals!! - Game.getHealFromKick(kickType)
                }
            }
            Account.KickType.PAIN_KICK -> {
            }
            Account.KickType.HEALS -> {
                when (secondPlayer?.kickType) {
                    Account.KickType.HEND, Account.KickType.LEG, Account.KickType.FINGER_IN_IES,
                    Account.KickType.KONTR_ATTACK, Account.KickType.PAIN_KICK -> {
                        firstPlayer?.heals = firstPlayer?.heals?.plus(Game.getHealFromKick(secondPlayer.kickType!!))
                    }
                    Account.KickType.HEALS -> {
                    }
                    null -> {
                    }
                }
            }
        }

        when (secondPlayer?.kickType) {
            Account.KickType.HEND, Account.KickType.LEG, Account.KickType.FINGER_IN_IES -> {
                secondPlayer.kickType?.let { kickType ->
                    when (firstPlayer?.kickType) {
                        Account.KickType.HEND,
                        Account.KickType.LEG,
                        Account.KickType.FINGER_IN_IES,
                        Account.KickType.PAIN_KICK -> {
                            firstPlayer?.heals = firstPlayer?.heals!! - Game.getHealFromKick(kickType)
                        }
                    }
                }
            }
            Account.KickType.KONTR_ATTACK -> {
                secondPlayer.kickType?.let { secondKickType ->
                    if (firstPlayer?.kickType !== Account.KickType.KONTR_ATTACK) firstPlayer?.heals = firstPlayer?.heals!! - Game.getHealFromKick(secondKickType)
                }
            }
            Account.KickType.PAIN_KICK -> {
            }
            Account.KickType.HEALS -> {
                when (firstPlayer?.kickType) {
                    Account.KickType.HEND, Account.KickType.LEG, Account.KickType.FINGER_IN_IES,
                    Account.KickType.KONTR_ATTACK, Account.KickType.PAIN_KICK -> {
                        secondPlayer?.heals = secondPlayer?.heals?.plus(Game.getHealFromKick(firstPlayer.kickType!!))
                    }
                    Account.KickType.HEALS -> {
                    }
                    null -> {
                    }
                }

            }
            null -> {
            }
        }
    }

    private fun calculateEnergy(player: Account) {
        player.energy = player.energy?.plus(Game.getEnergyFromKick(player.kickType))
    }

    private val endRoundHandler = HttpHandler { httpExchange ->
        run {
            when (httpExchange!!.requestMethod) {
                "POST" -> {
                    val inputStream = httpExchange.requestBody

                    val requestBody = streamToString(inputStream)
                    val jsonBody = JSONObject(requestBody)
                    val firstPlayerId = jsonBody.getString("firstPlayerId")
                    val secondPlayerId = jsonBody.getString("secondPlayerId")

                    val json = endRaund(firstPlayerId, secondPlayerId)
                    // save message to database

                    //for testing
                    sendResponse(httpExchange, json.toString())
                }
            }
        }
    }

    private fun endRaund(firstPlayerId: String?, secondPlayerId: String?): JSONObject {
        val firstPlayer = accounts?.get(firstPlayerId)
        val secondPlayer = accounts?.get(secondPlayerId)

        firstPlayer?.kickType = null
        secondPlayer?.kickType = null

        firstPlayer?.isMove = false
        secondPlayer?.isMove = false

        firstPlayer?.isEndRound = true

        accounts?.put(firstPlayer!!.name, firstPlayer)
        accounts?.put(secondPlayer!!.name, secondPlayer)

        val jsonObject = JSONObject()
        jsonObject.put("firstPlayer", firstPlayer?.toJson())
        jsonObject.put("secondPlayer", secondPlayer?.toJson())
        jsonObject.put("status", firstPlayer?.gameStatus?.status)
        return jsonObject
    }

    private fun startFight(firstPlayerId: String, secondPlayerId: String): JSONObject {
        val firstPlayer = accounts?.get(firstPlayerId)
        val secondPlayer = accounts?.get(secondPlayerId)

        firstPlayer?.kickType = null
        firstPlayer?.gameStatus = GameStatus.FIGHT
        secondPlayer?.kickType = null
        secondPlayer?.gameStatus = GameStatus.FIGHT

        firstPlayer?.let { accounts?.put(firstPlayer.name, it) }
        secondPlayer?.let { accounts?.put(secondPlayer.name, it) }

        val jsonObject = JSONObject()
        jsonObject.put("firstPlayer", firstPlayer?.toJson())
        jsonObject.put("secondPlayer", secondPlayer?.toJson())
        jsonObject.put("status", firstPlayer?.gameStatus?.status)
        return jsonObject
    }


    private val statusFightHandler = HttpHandler { httpExchange ->
        run {
            when (httpExchange!!.requestMethod) {
                "GET" -> {
                    val params: Map<String, String>? = queryToMap(httpExchange.requestURI.query)
                    val inputStream = httpExchange.requestBody

//                    val requestBody = streamToString(inputStream)
//                    val jsonBody = JSONObject(requestBody)
                    val firstPlayerId = params?.get("firstPlayerId")
                    val secondPlayerId = params?.get("secondPlayerId")

                    System.out.println("SER -" + "userName ${firstPlayerId} get status  ")


                    val json = status(firstPlayerId, secondPlayerId)
                    // save message to database

                    //for testing
                    sendResponse(httpExchange, json.toString())
                }
            }
        }
    }

    private fun status(firstPlayerId: String?, secondPlayerId: String?): JSONObject {
        val firstPlayer = accounts?.get(firstPlayerId)
        val secondPlayer = accounts?.get(secondPlayerId)

        val jsonObject = JSONObject()
        jsonObject.put("firstPlayer", firstPlayer?.toJson())
        jsonObject.put("secondPlayer", secondPlayer?.toJson())
        jsonObject.put("status", firstPlayer?.gameStatus?.status)
        return jsonObject
    }

    private val resetKicksHandler = HttpHandler { httpExchange ->
        run {
            when (httpExchange!!.requestMethod) {
                "POST" -> {
                    val inputStream = httpExchange.requestBody
                    val requestBody = streamToString(inputStream)
                    val jsonBody = JSONObject(requestBody)
                    val firstPlayerId = jsonBody?.getString("playerId")

                    System.out.println("SER -" + "userName ${firstPlayerId} refreshKicks  ")


                    val json = resetKicks(firstPlayerId)
                    // save message to database

                    //for testing
                    sendResponse(httpExchange, json.toString())
                }
            }
        }
    }

    private fun resetKicks(firstPlayerId: String?): JSONObject {
        val firstPlayer = accounts?.get(firstPlayerId)

        val ENERGY_FOR_RESSET = 3

        val isKen = firstPlayer?.energy ?: 0 >= ENERGY_FOR_RESSET

        if (isKen) {
            firstPlayer?.energy!! - ENERGY_FOR_RESSET
            firstPlayer?.let { accounts?.put(firstPlayer.name, it) }
        }

        val jsonObject = JSONObject()
        jsonObject.put("isSuccess", isKen)
        jsonObject.put("energy", firstPlayer?.energy)

        return jsonObject
    }


    private val endFightHandler = HttpHandler { httpExchange ->
        run {
            when (httpExchange!!.requestMethod) {
                "GET" -> {
                    val params: Map<String, String>? = queryToMap(httpExchange.requestURI.query)
                    val inputStream = httpExchange.requestBody

                    val playerId = params?.get("userName")

                    System.out.println("SER -" + "userName end game ${playerId} ")
                    val json = playerId?.let { endGame(it) }
                    // save message to database

                    //for testing
                    sendResponse(httpExchange, json.toString())
                }
            }
        }
    }

    private fun endGame(playerId: String): JSONObject {
        val firstPlayer = accounts?.get(playerId)
        firstPlayer?.heals = 500
        firstPlayer?.let { accounts?.put(playerId, it) }
        val jsonObject = JSONObject()

        return jsonObject
    }


  private val fightersListHandler = HttpHandler { httpExchange ->
        run {
            when (httpExchange!!.requestMethod) {
                "GET" -> {
                    val json = getFightersList()

                    sendResponse(httpExchange, json.toString())
                }
            }
        }
    }

    private fun getFightersList(): JSONObject {
        val jsonObject = JSONObject()
        val jsonArray = JSONArray()

        accounts?.forEach { t, u ->
            jsonArray.put(t)
        }

        jsonObject.put("data", jsonArray)

        return jsonObject
    }



    fun queryToMap(query: String): Map<String, String>? {
        val result: MutableMap<String, String> = HashMap()
        for (param in query.split("&".toRegex()).toTypedArray()) {
            val entry = param.split("=".toRegex()).toTypedArray()
            if (entry.size > 1) {
                result[entry[0]] = entry[1]
            } else {
                result[entry[0]] = ""
            }
        }
        return result
    }

}