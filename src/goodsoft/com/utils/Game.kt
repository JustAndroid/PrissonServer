package goodsoft.com.utils

import goodsoft.com.models.Account

object Game {

    fun getHealFromKick(kickType: Account.KickType?): Int {
        return when (kickType) {
            Account.KickType.HEND -> 50
            Account.KickType.LEG -> 125
            Account.KickType.FINGER_IN_IES -> 50
            Account.KickType.KONTR_ATTACK -> 50
            Account.KickType.PAIN_KICK -> 35
            Account.KickType.HEALS -> 0
            else -> 0
        }
    }

    fun getEnergyFromKick(kickType: Account.KickType?): Int {
        return when (kickType) {
            Account.KickType.LEG -> -3
            Account.KickType.FINGER_IN_IES -> 2
            Account.KickType.HEND -> 3
            Account.KickType.KONTR_ATTACK -> -4
            Account.KickType.PAIN_KICK -> -4
            Account.KickType.HEALS -> -4
            else -> 0
        }
    }
}

//Взбодриться, лечить себя в размере полученного урона

// удар ногой 125

// просто удар, добавляет бодряк 50 + 3 бодряка

//контр атака уклоняешся и наносишь урон 50  -3

//пальцем в глаз +2 (наносишь урон и не даешь восстановить бодряки) кроме того восстанавливаешь себе

// даеться 3 бодряка (восстановление всех ударов 3 бодряка)

// удар по болевой/ против взбодриться 35 урона -4

// жизни 500

//бодряки энергия