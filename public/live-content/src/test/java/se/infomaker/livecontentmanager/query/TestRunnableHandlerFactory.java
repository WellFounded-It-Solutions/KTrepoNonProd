package se.infomaker.livecontentmanager.query;

import se.infomaker.livecontentmanager.query.TestRunnableHandler;
import se.infomaker.livecontentmanager.query.runnable.RunnableHandler;
import se.infomaker.livecontentmanager.query.runnable.RunnableHandlerFactory;

public class TestRunnableHandlerFactory implements RunnableHandlerFactory {
    @Override
    public RunnableHandler create() {
        return new TestRunnableHandler();
    }
}
