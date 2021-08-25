package goodsoft.com.models

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import org.json.JSONObject

@DatabaseTable(tableName = "accounts")
class Account {
    @DatabaseField(id = true)
    var name: String = ""

    @DatabaseField
    var secondPlayerName: String? = null

    @DatabaseField
    var gameStatus: GameStatus? = null

    @DatabaseField
    var isMove: Boolean = false

    @DatabaseField
    var isEndRound: Boolean = false

    @DatabaseField
    var password: String? = null

    @DatabaseField
    var heals: Int? = null //500

    @DatabaseField
    var energy: Int? = null

    var kickType: KickType? = KickType.HEND

    var enableHits = EnableKick()


    constructor(
            name: String,
            secondPlayerName: String?,
            isMove: Boolean,
            isEndRound: Boolean,
            password: String? = null,
            heals: Int? = null,
            energy: Int? = null,
            kickType: KickType? = KickType.HEND
    ) {
        this.name = name
        this.secondPlayerName = secondPlayerName
        this.isMove = isMove
        this.isEndRound = isEndRound
        this.password = password
        this.heals = heals
        this.energy = energy
        this.kickType = kickType
    }

    constructor() {
    }

    fun toJson(): JSONObject {
        var jsonObject = JSONObject()
        jsonObject.put("name", name)
        jsonObject.put("secondPlayerName", secondPlayerName)
        jsonObject.put("isMove", isMove)
        jsonObject.put("isEndRound", isEndRound)
        jsonObject.put("heals", heals)
        jsonObject.put("energy", energy)
        jsonObject.put("kickType", kickType?.type ?: "")
        jsonObject.put("enableKicks", enableHits.toJson())
        return jsonObject
    }

    enum class KickType(val type: String) {
        HEND("hend"),
        LEG("leg"),
        FINGER_IN_IES("finger_in_ies"),
        KONTR_ATTACK("kontr_attack"),
        PAIN_KICK("pain_kick"),
        HEALS("heals"),
    }


    class EnableKick{
        var isEnableLeg: Boolean = true
        var isEnableKontrAttack: Boolean = true
        var isEnablePainKick: Boolean = true
        var isEnableHeals: Boolean = true

        fun toJson(): JSONObject {
            var jsonObject = JSONObject()
            jsonObject.put("is_enableLeg", isEnableLeg)
            jsonObject.put("is_enable_kontr_attack", isEnableKontrAttack)
            jsonObject.put("is_enable_pain_kick", isEnablePainKick)
            jsonObject.put("is_enable_heals", isEnableHeals)
            return jsonObject
        }
    }


}