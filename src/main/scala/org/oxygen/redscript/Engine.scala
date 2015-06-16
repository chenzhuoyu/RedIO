package org.oxygen.redscript

import org.oxygen.redscript.objects.{RedCode, RedObject}

object Engine
{
	@native def init(args: Array[String]): Boolean
	@native def cleanup(): Unit

	@native def compile(script: String): RedCode
	@native def addObject(name: String, `object`: Any): RedObject

	@native def registerCurrentThread()
	@native def unregisterCurrentThread()
}
