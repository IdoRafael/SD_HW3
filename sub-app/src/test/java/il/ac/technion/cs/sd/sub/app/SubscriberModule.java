package il.ac.technion.cs.sd.sub.app;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;
import il.ac.technion.cs.sd.sub.library.Reader;
import il.ac.technion.cs.sd.sub.library.ReaderFactory;

// This module is in the testing project, so that it could easily bind all dependencies from all levels.
public class SubscriberModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(SubscriberInitializer.class).to(SubscriberInitializerImpl.class);
    bind(SubscriberReader.class).to(SubscriberReaderImpl.class);

    install(new FactoryModuleBuilder()
            .build(ReaderFactory.class));

    bind(String.class)
            .annotatedWith(Names.named("productsAndPricesFileName"))
            .toInstance("0");

    bind(String.class)
            .annotatedWith(Names.named("usersAndOrdersFileName"))
            .toInstance("1");

    bind(String.class)
            .annotatedWith(Names.named("ordersAndProductsFileName"))
            .toInstance("2");

    bind(String.class)
            .annotatedWith(Names.named("ordersAndHistoryFileName"))
            .toInstance("3");

    bind(String.class)
            .annotatedWith(Names.named("productsAndOrdersFileName"))
            .toInstance("4");

    bind(String.class)
            .annotatedWith(Names.named("usersAndProductsFileName"))
            .toInstance("5");

    bind(String.class)
            .annotatedWith(Names.named("productsAndUsersFileName"))
            .toInstance("6");
  }
}
