package modules

import com.google.inject.AbstractModule
import java.time.Clock

import net.codingwell.scalaguice.ScalaModule
import services.{ApplicationTimer, AtomicCounter, Counter}

class MainModule extends AbstractModule with ScalaModule {
  def configure() = {
    bind[Clock].toInstance(Clock.systemDefaultZone)
    bind[ApplicationTimer].asEagerSingleton()
    bind[Counter].to[AtomicCounter]
  }
}
