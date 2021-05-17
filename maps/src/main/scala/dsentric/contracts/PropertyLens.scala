package dsentric.contracts

import dsentric.codecs.DCodec
import dsentric.{RawObject, _}
import dsentric.failure._

private[dsentric] trait PropertyLens[D <: DObject, T] extends BaseAux with ParameterisedAux[D] {
  def _key:String
  def _path:Path
  def _codec: DCodec[T]
  def _parent: BaseContract[D]
  def _root: ContractFor[D] = _parent._root

  /**
   * Return the property value or failure
   * Can return Default value if provided value is empty or invalid and type behaviour is empty.
   * Import to keep awareness of Traversed as path modifiers shouldnt fail if PathEmptyMaybe
   * @param obj
   * @return
   */
  private[contracts] def __get(data:D, dropBadTypes:Boolean):Traversed[T]

  /**
   * Verifies the direct property against the object.
   * Will remove from object if Not Found or Null
   * Empty objects for Object structures will also get removed.
   * @param obj
   * @return
   */
  private[contracts] def __reduce(obj: RawObject, dropBadTypes:Boolean):ValidStructural[RawObject]


  private[contracts] def __reduceDelta(deltaObject:RawObject, currentValue:RawObject, dropBadTypes:Boolean):ValidResult[RawObject]
//  /**
//   * Verifies the property against the delta property value.
//   * Is not responsible for traversal.
//   * @param deltaValue
//   * @param currentValue
//   * @return
//   */
//  private[contracts] def __reduceDeltaTTrav(deltaValue:Raw, currentValue:Option[Raw]):DeltaReduce[Raw]

  private[contracts] def __set(obj:D, value:T):D = {
    _codec(value) match {
      case r:RawObject@unchecked if r.isEmpty =>
        obj.internalWrap(PathLensOps.drop(obj.value, _path).getOrElse(Map.empty)).asInstanceOf[D]
      case r =>
        obj.internalWrap(PathLensOps.set(obj.value, _path, r)).asInstanceOf[D]
    }

  }

  /**
   * Does the object satisfy all the type and expectation constraints.
   * Returns list of possible failures.
   * @param obj
   * @return
   */
  def $verify(obj:D):List[StructuralFailure] =
    __get(obj, false) match {
      case Failed(head, tail) =>
        head :: tail
      case _ =>
        Nil
    }

  /**
   * Sets or Replaces value for the Property.
   * Will create object path to Property if objects dont exist.
   * Function doesnt provided validation of the value being set.
   * @param value
   * @return
   */
  final def $set(value:T):PathSetter[D] =
    ValueSetter(_path, _codec(value))
}


//final class ExpectedDelta[D <: DObject, T] private[dsentric](property:ExpectedLens[D, T]) {
//  def $get(delta:DObject):ValidResult[Option[T]] =
//    property.__incorrectTypeBehaviour.traverse(delta.value, property)
//
//  def $set(value:T):PathSetter[DObject] =
//    ValueSetter[DObject](property._path, property._codec(value).value)
//
//  def $setOrDrop(value:Option[T]):PathSetter[DObject] =
//    value.fold[PathSetter[DObject]](ValueDrop(property._path))(v => ValueSetter(property._path, property._codec(v).value))
//
//  def $modify(f:Option[T] => T):ValidPathSetter[DObject] =
//    MaybeModifySetter[DObject, T](obj => property.__incorrectTypeBehaviour.traverse(obj.value, property), f, (d, t) =>  $set(t)(d))
//
//  def $modifyOrDrop(f:Option[T] => Option[T]):ValidPathSetter[DObject] =
//    ModifyOrDropSetter[DObject, T](obj => property.__incorrectTypeBehaviour.traverse(obj.value, property), f, (d, mt) => $setOrDrop(mt)(d))
//
//  def $drop:PathSetter[DObject] =
//    ValueDrop[DObject](property._path)
//
//  def unapply(delta:DObject):Option[Option[T]] =
//    property.__incorrectTypeBehaviour.matcher(delta.value, property)
//}
//
//final class MaybeDelta[D <: DObject, T] private[dsentric](property:PropertyLens[D, T]) {
//  private val _deltaCodec = PessimisticCodecs.dNullableCodec[T](property._codec)
//
//  def $get(delta:DObject):ValidResult[Option[DNullable[T]]] =
//    property.__incorrectTypeBehaviour.traverse(delta.value, property._root, property._path, _deltaCodec)
//
//  def $set(value:T):PathSetter[DObject] =
//    ValueSetter[DObject](property._path, property._codec(value).value)
//
//  def $drop:PathSetter[DObject] =
//    ValueDrop[DObject](property._path)
//
//  def $setOrDrop(value:Option[T]):PathSetter[DObject] =
//    value.fold[PathSetter[DObject]](ValueDrop(property._path))(v => ValueSetter(property._path, property._codec(v).value))
//
//  def $setNull:PathSetter[DObject] =
//    ValueSetter[DObject](property._path, DNull)
//
//  def $setOrNull(value:Option[DNullable[T]]):PathSetter[DObject] =
//    value.fold[PathSetter[DObject]](ValueDrop(property._path))(v => ValueSetter(property._path, _deltaCodec(v).value))
//
//  def $modify(f:Option[T] => T):ValidPathSetter[DObject] =
//    MaybeModifySetter[DObject, T](obj => property.__incorrectTypeBehaviour.traverse(obj.value, property), f, (d, t) => $set(t)(d))
//
//  def $modifyOrDrop(f:Option[T] => Option[T]):ValidPathSetter[DObject] =
//    ModifyOrDropSetter[DObject, T](obj => property.__incorrectTypeBehaviour.traverse(obj.value, property), f, (d, mt) => $setOrDrop(mt)(d))
//
//  def $modifyOrNull(f:Option[DNullable[T]] => Option[DNullable[T]]):ValidPathSetter[DObject] =
//    ModifyOrDropSetter[DObject, DNullable[T]](obj => property.__incorrectTypeBehaviour.traverse(obj.value, property._root, property._path, _deltaCodec), f, (d, mt) => $setOrNull(mt)(d))
//
//  def unapply(delta:DObject):Option[Option[DNullable[T]]] =
//    property.__incorrectTypeBehaviour.matcher(delta.value, _deltaCodec)
//}


