package org.oxygen.redio.runtime

import java.util.concurrent.ThreadFactory

object ThreadManager extends ThreadFactory
{
	override def newThread(task: Runnable): Thread = new ScriptTask(task)
}
