package org.oxygen.redio.runtime

import java.util.concurrent.{Callable, Executors, TimeUnit, TimeoutException}

import org.oxygen.redio.RedIO
import org.oxygen.redio.common.Utils
import org.oxygen.redscript.{Printer, Engine}

object ScriptEngine
{
	trait Executor
	{
		private val threadPool = Executors.newSingleThreadExecutor(ThreadManager)

		def execute(function: => Unit) = try
		{
			threadPool.submit(new Callable[Any]
			{
				override def call(): Any = try
				{
					function
				} catch
				{
					case e: Exception =>
						e.printStackTrace()
						null
				}
			}).get(500, TimeUnit.MILLISECONDS)
		} catch
		{
			case e: TimeoutException => null
			case e: Exception =>
				e.printStackTrace()
				null
		}
	}

    object PrinterImpl extends Printer
    {
        override def print(text: String): Unit = RedIO.logger.info(text)
        override def println(text: String): Unit = RedIO.logger.info(text)
    }

	def init() =
	{
		RedIO.logger.info("Starting script engine for " + System.getProperty("os.name"))
		System.getProperty("os.name") match
		{
			case "Linux"						=> Utils.loadLibrary("/natives/librssb.so")
			case "Mac OS X"						=> Utils.loadLibrary("/natives/librssb.dylib")
			case x if x.startsWith("Windows")	=> Utils.loadLibrary("/natives/librssb.dll")
			case _								=> throw new RuntimeException("This operating system is not supported")
		}

		Engine.init(Array("redio"))
        Engine.printers += PrinterImpl
	}
}
