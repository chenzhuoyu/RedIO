package org.oxygen.redscript

import org.oxygen.redscript.objects.{RedCode, RedObject}

import scala.collection.mutable

object Engine
{
    val printers = mutable.MutableList[Printer]()

	@native def init(args: Array[String]): Boolean
	@native def cleanup(): Unit

	@native def compile(script: String): RedCode
	@native def addObject(name: String, `object`: Any): RedObject

	@native def registerCurrentThread()
	@native def unregisterCurrentThread()

    def print(text: String)   = for (p <- printers) p.print(text)
    def println(text: String) = for (p <- printers) p.println(text)
}
