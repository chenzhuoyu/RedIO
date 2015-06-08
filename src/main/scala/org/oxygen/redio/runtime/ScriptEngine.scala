package org.oxygen.redio.runtime

import java.util.concurrent.Executors

import org.oxygen.redscript.Engine

object ScriptEngine
{
	val threadPool = Executors.newCachedThreadPool(ThreadManager)

	System.loadLibrary("rssb")
	Engine.init(Array("redio"))

	def execute(function: () => Unit) =
	{
		threadPool.execute(new Runnable
		{
			override def run(): Unit = try
			{
				function()
			} catch
			{
				case e: Exception => e.printStackTrace()
			}
		})
	}
}
