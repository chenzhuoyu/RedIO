package org.oxygen.redscript.objects

class RedObject private[objects] ()
{
	/* This field stores the C "RedObject *" pointer */
	val objectPtr: Long = 0L

	@native def str: String
	@native def repr: String

	@native def length: Int
	@native def fields: Array[String]

	@native def isNull: Boolean
	@native def isTrue: Boolean
	@native def isCallable: Boolean
	@native def isInstanceOf(`type`: RedType): Boolean

	@native def delIndex(key: Any): Unit
	@native def getIndex(key: Any): Any
	@native def setIndex(key: Any, value: Any): Unit

	@native def delAttrib(name: String): Unit
	@native def getAttrib(name: String): Any
	@native def setAttrib(name: String, value: Any): Unit

	@native def apply(args: Any*): Any
	@native def applyOn(method: String, args: Any*): Any

	@native def ref: RedObject
	@native def unref(): Unit

	override def toString = repr
	override def finalize() = try unref() catch { case _: Throwable => }
}
