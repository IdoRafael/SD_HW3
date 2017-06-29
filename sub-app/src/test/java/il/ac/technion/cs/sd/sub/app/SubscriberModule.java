package il.ac.technion.cs.sd.sub.app;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;
import il.ac.technion.cs.sd.sub.library.Reader;
import il.ac.technion.cs.sd.sub.library.ReaderFactory;
import il.ac.technion.cs.sd.sub.library.ReaderImpl;

// This module is in the testing project, so that it could easily bind all dependencies from all levels.
public class SubscriberModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(SubscriberInitializer.class).to(SubscriberInitializerImpl.class);
    bind(SubscriberReader.class).to(SubscriberReaderImpl.class);

    install(new FactoryModuleBuilder()
            .implement(Reader.class, ReaderImpl.class)
            .build(ReaderFactory.class));

    bind(String.class)
            .annotatedWith(Names.named("usersFileName"))
            .toInstance("0");

    bind(String.class)
            .annotatedWith(Names.named("usersJournalsFileName"))
            .toInstance("1");

    bind(String.class)
            .annotatedWith(Names.named("journalsFileName"))
            .toInstance("2");

    bind(String.class)
            .annotatedWith(Names.named("journalsUsersFileName"))
            .toInstance("3");
  }
}
