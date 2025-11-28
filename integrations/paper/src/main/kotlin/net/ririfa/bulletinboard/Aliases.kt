package net.ririfa.bulletinboard

val Plugin by lazy { BulletinBoard.instance }
val LM by lazy { BulletinBoard.langMan }
val DB by lazy { BulletinBoard.dataBase }
val T by lazy { BulletinBoard.thread }

val LangDir by lazy { BulletinBoard.instance.langDir }
val DBDir by lazy { BulletinBoard.instance.dbDir }