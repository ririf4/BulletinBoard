package net.ririfa.bulletinboard

typealias DB = DataBase

val Plugin by lazy { BulletinBoard.instance }
val LM by lazy { BulletinBoard.langMan }
val T by lazy { BulletinBoard.thread }

val LangDir by lazy { BulletinBoard.instance.langDir }
val DBDir by lazy { BulletinBoard.instance.dbDir }

val Logger by lazy { BulletinBoard.logger }