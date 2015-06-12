package org.oxygen.redio.runtime

import org.oxygen.redscript.Engine

class ScriptTask(val task: Runnable) extends Thread
{
	override def run(): Unit = try
	{
		Engine.registerCurrentThread()
		task.run()
	} finally
	{
		Engine.unregisterCurrentThread()
	}
}
