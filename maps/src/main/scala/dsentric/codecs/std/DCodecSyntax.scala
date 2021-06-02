package dsentric.codecs.std

import dsentric.DObject
import dsentric.codecs.{DCodec, DContractCodec, DStringCodec}
import dsentric.contracts.Contract
import scala.reflect.ClassTag

//Dunno if named correct
trait DCodecSyntax {

  implicit def contract2MapCodec[K](contract:Contract)(implicit D:DStringCodec[K]):DCodec[Map[K, DObject]] =
    DMapCodecs.keyValueMapCodec(D, DContractCodec(contract))

  implicit def contract2Codec(contract:Contract):DCodec[DObject] =
    DContractCodec(contract)

  implicit def contract2VectorCodec(contract:Contract):DCodec[Vector[DObject]] =
    DCollectionCodecs.vectorCodec(DContractCodec(contract))

  implicit def contract2ArrayCodec(contract:Contract):DCodec[Array[DObject]] =
    DCollectionCodecs.arrayCodec(DContractCodec(contract), implicitly[ClassTag[DObject]])

  implicit def contract2ListCodec(contract:Contract):DCodec[List[DObject]] =
    DCollectionCodecs.listCodec(DContractCodec(contract))
}
