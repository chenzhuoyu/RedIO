package org.oxygen.redio.runtime

import java.util.concurrent.{Callable, Executors, TimeUnit, TimeoutException}

import org.oxygen.redscript.Engine

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

	def init() =
	{
		System.loadLibrary("rssb")
		Engine.init(Array("redio"))
	}
}
