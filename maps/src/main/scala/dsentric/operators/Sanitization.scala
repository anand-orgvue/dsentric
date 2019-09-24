package dsentric.operators

import dsentric._
import dsentric.contracts.BaseContract

object Sanitization {

  def sanitize[D <: DObject](contract:BaseContract[D], value:D):D = {
    contract._fields.foldLeft(value){
      case (v, (field, property)) =>
        property._dataOperators.collectFirst{ case s:Sanitizer[_] => s} match {
          case None =>
            v
          case Some(s) =>
            def f = (d:Option[Raw]) => PathResult(s.sanitize(d.map(dd => property._strictness.apply(dd, property._codec)).getOrElse(Empty)))
            PathLensOps.maybeModifyOrDrop(v.value, contract._path, f)
              .fold(v)(v.internalWrap(_).asInstanceOf[D])

        }
    }
  }

}
