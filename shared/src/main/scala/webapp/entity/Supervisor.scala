package webapp.entity

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import kofre.base.*
import kofre.datatypes.*
import kofre.datatypes.LastWriterWins.TimedVal
import kofre.datatypes.alternatives.MultiValueRegister
import kofre.syntax.*
import kofre.time.VectorClock
import webapp.Codecs.*
import webapp.webrtc.DeltaFor

case class Supervisor(
    _firstName: Attribute[String] = Attribute.empty,
    _lastName: Attribute[String] = Attribute.empty,
    _eMail: Attribute[String] = Attribute.empty,
    _exists: Attribute[Boolean] = Attribute.empty,
) extends Entity[Supervisor]
    derives DecomposeLattice,
      Bottom {

  def exists: Attribute[Boolean] = _exists

  def identifier: Attribute[String] = _firstName

  def withExists(exists: Boolean): Supervisor = {
    this.copy(_exists = _exists.set(exists))
  }

}

object Supervisor {
  val empty: Supervisor = Supervisor()

  implicit val codec: JsonValueCodec[Supervisor] = JsonCodecMaker.make(CodecMakerConfig.withMapAsArray(true))

  implicit val deltaCodec: JsonValueCodec[DeltaFor[Supervisor]] = JsonCodecMaker.make
}
