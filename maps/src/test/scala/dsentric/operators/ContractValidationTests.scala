package dsentric.operators

import dsentric._
import dsentric.contracts.ClosedFields
import dsentric.failure._
import org.scalatest.{FunSpec, Matchers}

class ContractValidationTests extends FunSpec with Matchers {

  import Dsentric._
  import PessimisticCodecs._

  describe("Contract validation") {
    describe("Contract structure") {
      object Empty extends Contract

      it("Should validate an empty Contract") {
        Empty.$ops.validate(DObject.empty) shouldBe ValidationFailures.empty
        Empty.$ops.validate(DObject("key" := "value")) shouldBe ValidationFailures.empty
      }
      it("Should validate deltas") {
        Empty.$ops.validate(DObject("key" := "value"), DObject("key" := 123)) shouldBe ValidationFailures.empty
        Empty.$ops.validate(DObject("key" := DNull), DObject("key" := 123)) shouldBe ValidationFailures.empty
      }
    }
    describe("Closed contract structure") {
      object EmptyClosed extends Contract with ClosedFields

      it("Should validate an empty contract") {
        EmptyClosed.$ops.validate(DObject.empty) shouldBe ValidationFailures.empty
      }
      it("Should fail on any other field") {
        EmptyClosed.$ops.validate(DObject("field" := false)) should contain (ClosedContractFailure(EmptyClosed, Path.empty, "field"))
      }
      it("Should fail on delta value") {
        EmptyClosed.$ops.validate(DObject("field" := false), DObject.empty) should contain (ClosedContractFailure(EmptyClosed, Path.empty, "field"))
      }
      it("Should succeed on removing a field") {
        EmptyClosed.$ops.validate(DObject("field" := DNull), DObject("field" := true)) shouldBe ValidationFailures.empty
      }
    }
  }

  describe("Expected field validation") {
    describe("Expected field structure") {
      object ExpectedField extends Contract {
        val exp = \[String]
      }
      it("Should fail if field not found") {
        ExpectedField.$ops.validate(DObject.empty) should contain(ExpectedFailure(ExpectedField.exp))
      }
      it("Should fail if field is of wrong type") {
        ExpectedField.$ops.validate(DObject("exp" := false)) should contain(IncorrectTypeFailure(ExpectedField.exp, false))
      }
      it("Should succeed if field exists and is of correct type") {
        ExpectedField.$ops.validate(DObject("exp" := "test")) shouldBe ValidationFailures.empty
      }
      it("Should fail if delta and field is empty") {
        ExpectedField.$ops.validate(DObject.empty, DObject.empty) should contain(ExpectedFailure(ExpectedField.exp))
      }
      it("Should fail if delta is incorrect type") {
        ExpectedField.$ops.validate(DObject("exp" := false), DObject("exp" := "test")) should contain(IncorrectTypeFailure(ExpectedField.exp, false))
      }
      it("Should succeed if delta is correct type") {
        ExpectedField.$ops.validate(DObject("exp" := "test"), DObject("exp" := "test2")) shouldBe ValidationFailures.empty
      }
      it("Should succeed if delta is correct type and state is incorrect") {
        ExpectedField.$ops.validate(DObject("exp" := "test"), DObject("exp" := false)) shouldBe ValidationFailures.empty
      }
      it("Should succeed if delta is empty and state is incorrect") {
        ExpectedField.$ops.validate(DObject.empty, DObject("exp" := false)) shouldBe ValidationFailures.empty
      }
      it("Should fail if delta is null") {
        ExpectedField.$ops.validate(DObject("exp" := DNull), DObject("exp" := "test")) should contain(ExpectedFailure(ExpectedField.exp))
      }
    }
    describe("Expected field with validator") {
      object ExpectedValidator extends Contract {
        val expGT = \[Int](Validators.>(5))
      }
      it("Should succeed if value is valid") {
        ExpectedValidator.$ops.validate(DObject("expGT" := 6)) shouldBe ValidationFailures.empty
      }
      it("Should fail if value is invalid") {
        ExpectedValidator.$ops.validate(DObject("expGT" := 3)) should contain(NumericalFailure(ExpectedValidator, Path("expGT"), 3, 5, "greater than"))
      }
      it("Should succeed if delta is valid and state is invalid") {
        ExpectedValidator.$ops.validate(DObject("expGT" := 6), DObject("expGT" := 3)) shouldBe ValidationFailures.empty
      }
      it("Should succeed if delta is valid and state is valid") {
        ExpectedValidator.$ops.validate(DObject("expGT" := 6), DObject("expGT" := 7)) shouldBe ValidationFailures.empty
      }
      it("Should fail if delta is invalid") {
        ExpectedValidator.$ops.validate(DObject("expGT" := 3), DObject("expGT" := 7)) should contain(NumericalFailure(ExpectedValidator, Path("expGT"), 3, 5, "greater than"))
      }
    }
  }

  describe("Maybe field validation") {
    describe("Maybe field structure") {
      object MaybeField extends Contract {
        val myb = \?[Long]
      }
      it("Should succeed if field not found") {
        MaybeField.$ops.validate(DObject.empty) shouldBe ValidationFailures.empty
      }
      it("Should fail if field is of wrong type") {
        MaybeField.$ops.validate(DObject("myb" := false)) should contain(IncorrectTypeFailure(MaybeField.myb, false))
      }
      it("Should succeed if field exists and is of correct type") {
        MaybeField.$ops.validate(DObject("myb" := 434)) shouldBe ValidationFailures.empty
      }
      it("Should succeed if delta and field is empty") {
        MaybeField.$ops.validate(DObject.empty, DObject.empty) shouldBe ValidationFailures.empty
      }
      it("Should fail if delta is incorrect type") {
        MaybeField.$ops.validate(DObject("myb" := false), DObject("myb" := 1324)) should contain(IncorrectTypeFailure(MaybeField.myb, false))
      }
      it("Should succeed if delta is correct type") {
        MaybeField.$ops.validate(DObject("myb" := 4123), DObject("myb" := 432)) shouldBe ValidationFailures.empty
      }
      it("Should succeed if delta is correct type and state is incorrect") {
        MaybeField.$ops.validate(DObject("myb" := 1234), DObject("myb" := false)) shouldBe ValidationFailures.empty
      }
      it("Should succeed if delta is empty and state is incorrect") {
        MaybeField.$ops.validate(DObject.empty, DObject("myb" := false)) shouldBe ValidationFailures.empty
      }
      it("Should succeed if delta is null") {
        MaybeField.$ops.validate(DObject("myb" := DNull), DObject("myb" := "test")) shouldBe ValidationFailures.empty
      }
    }
    describe("Maybefield with validator") {
      object MaybeValidator extends Contract {
        val mybGT = \?[Int](Validators.>(5))
      }
      it("Should succeed if value is valid") {
        MaybeValidator.$ops.validate(DObject("mybGT" := 6)) shouldBe ValidationFailures.empty
      }
      it("Should fail if value is invalid") {
        MaybeValidator.$ops.validate(DObject("mybGT" := 3)) should contain(NumericalFailure(MaybeValidator, Path("mybGT"), 3, 5, "greater than"))
      }
      it("Should succeed if delta is valid and state is invalid") {
        MaybeValidator.$ops.validate(DObject("mybGT" := 6), DObject("mybGT" := 3)) shouldBe ValidationFailures.empty
      }
      it("Should succeed if delta is valid and state is valid") {
        MaybeValidator.$ops.validate(DObject("mybGT" := 6), DObject("mybGT" := 7)) shouldBe ValidationFailures.empty
      }
      it("Should fail if delta is invalid") {
        MaybeValidator.$ops.validate(DObject("mybGT" := 3), DObject("mybGT" := 7)) should contain(NumericalFailure(MaybeValidator, Path("mybGT"), 3, 5, "greater than"))
      }
    }
  }

  describe("Default field validation") {
    describe("Default field structure") {
      object DefaultField extends Contract {
        val dfl = \![Long](4353)
      }
      it("Should succeed if field not found") {
        DefaultField.$ops.validate(DObject.empty) shouldBe ValidationFailures.empty
      }
      it("Should fail if field is of wrong type") {
        DefaultField.$ops.validate(DObject("dfl" := false)) should contain(IncorrectTypeFailure(DefaultField.dfl, false))
      }
      it("Should succeed if field exists and is of correct type") {
        DefaultField.$ops.validate(DObject("dfl" := 5312)) shouldBe ValidationFailures.empty
      }
      it("Should succeed if delta and field is empty") {
        DefaultField.$ops.validate(DObject.empty, DObject.empty) shouldBe ValidationFailures.empty
      }
      it("Should fail if delta is incorrect type") {
        DefaultField.$ops.validate(DObject("dfl" := false), DObject("dfl" := 1324)) should contain(IncorrectTypeFailure(DefaultField.dfl, false))
      }
      it("Should succeed if delta is correct type") {
        DefaultField.$ops.validate(DObject("dfl" := 123), DObject("dfl" := 5122)) shouldBe ValidationFailures.empty
      }
      it("Should succeed if delta is correct type and state is incorrect") {
        DefaultField.$ops.validate(DObject("dfl" := 5321), DObject("dfl" := false)) shouldBe ValidationFailures.empty
      }
      it("Should succeed if delta is empty and state is incorrect") {
        DefaultField.$ops.validate(DObject.empty, DObject("dfl" := false)) shouldBe ValidationFailures.empty
      }
      it("Should succeed if delta is null") {
        DefaultField.$ops.validate(DObject("dfl" := DNull), DObject("dfl" := "test")) shouldBe ValidationFailures.empty
      }
    }
    describe("Defaultfield with validator") {
      object DefaultValidator extends Contract {
        val dflGT = \?[Int](Validators.>(5))
      }
      it("Should succeed if value is valid") {
        DefaultValidator.$ops.validate(DObject("dflGT" := 6)) shouldBe ValidationFailures.empty
      }
      it("Should fail if value is invalid") {
        DefaultValidator.$ops.validate(DObject("dflGT" := 3)) should contain(NumericalFailure(DefaultValidator, Path("dflGT"), 3, 5, "greater than"))
      }
      it("Should succeed if delta is valid and state is invalid") {
        DefaultValidator.$ops.validate(DObject("dflGT" := 6), DObject("dflGT" := 3)) shouldBe ValidationFailures.empty
      }
      it("Should succeed if delta is valid and state is valid") {
        DefaultValidator.$ops.validate(DObject("dflGT" := 6), DObject("dflGT" := 7)) shouldBe ValidationFailures.empty
      }
      it("Should fail if delta is invalid") {
        DefaultValidator.$ops.validate(DObject("dflGT" := 3), DObject("dflGT" := 7)) should contain(NumericalFailure(DefaultValidator, Path("dflGT"), 3, 5, "greater than"))
      }
    }
  }

  describe("Expected object validation") {

    describe("Nested object structure") {
      object ExpectedEmpty extends Contract {
        val nested = new \\ {}
      }
      object ExpectedExpected extends Contract {
        val nested = new \\ {
          val exp = \[String]
        }
      }
      object ExpectedMaybe extends Contract {
        val nested = new \\ {
          val myb = \?[String]
        }
      }
      object ExpectedClosed extends Contract {
        val nested = new \\ with ClosedFields {
          val myb = \?[String]
        }
      }
      it("Should be valid if object is empty and no expected properties") {
        ExpectedEmpty.$ops.validate(DObject.empty) shouldBe ValidationFailures.empty
      }
      it("Should be valid if object is empty and only maybe properties") {
        ExpectedMaybe.$ops.validate(DObject.empty) shouldBe ValidationFailures.empty
      }
      it("Should fail if object is empty and has expected properties") {
        ExpectedExpected.$ops.validate(DObject.empty) should contain(ExpectedFailure(ExpectedExpected.nested.exp))
      }
      it("Should fail if object is not an object") {
        ExpectedExpected.$ops.validate(DObject("nested" := 123)) should contain(IncorrectTypeFailure(ExpectedExpected.nested, 123))
      }
      it("Should fail if closed and has extra properties") {
        ExpectedClosed.$ops.validate(DObject("nested" ::= ("extra" := 123))) should contain(ClosedContractFailure(ExpectedClosed, ExpectedClosed.nested._path, "extra"))
      }
      it("Should be valid if closed and contains only included valid properties") {
        ExpectedClosed.$ops.validate(DObject("nested" ::= ("myb" := "value"))) shouldBe ValidationFailures.empty
      }
      describe("with deltas") {
        it("Should succeed if nested is null and object has no expected properties") {
          ExpectedEmpty.$ops.validate(DObject("nested" := DNull), DObject("nested" ::= ("value" := 123))) shouldBe ValidationFailures.empty
          ExpectedMaybe.$ops.validate(DObject("nested" := DNull), DObject("nested" ::= ("myb" := "value"))) shouldBe ValidationFailures.empty
        }
        it("Should fail if nested is null and object has expected properties") {
          ExpectedExpected.$ops.validate(DObject("nested" := DNull), DObject("nested" ::= ("exp" := "value"))) should contain(ExpectedFailure(ExpectedExpected.nested.exp))
        }
        it("Should fail if contents fail") {
          ExpectedExpected.$ops.validate(DObject("nested" ::= ("exp" := DNull)), DObject("nested" ::= ("exp" := "value"))) should contain(ExpectedFailure(ExpectedExpected.nested.exp))
        }
        it("Should succeed if contents succeed") {
          ExpectedExpected.$ops.validate(DObject("nested" ::= ("exp" := "value2")), DObject("nested" ::= ("exp" := "value"))) shouldBe ValidationFailures.empty
        }
      }
    }
    describe("Nested object validation") {
      object ExpectedValid extends Contract {
        val noRemoval = new \\(Validators.noKeyRemoval) {
          val myb = \?[String](Validators.nonEmptyOrWhiteSpace)
        }
        val oneOrTwo = new \\(Validators.minLength(1) && Validators.maxLength(2)) {}
      }
      it("Should succeed if object validation succeeds") {
        ExpectedValid.$ops.validate(DObject("noRemoval" ::= ("myb" := "value"), "oneOrTwo" ::= ("value" := false)))
      }
      it("Should fail if empty expected object would fail and no object value provided") {
        ExpectedValid.$ops.validate(DObject.empty) should contain(MinimumLengthFailure(ExpectedValid, ExpectedValid.oneOrTwo._path, 1, 0))
      }
      it("Should fail if object validation fails") {
        ExpectedValid.$ops.validate(DObject("oneOrTwo" ::= ("value" := false, "value2" := 123, "value3" := "v"))) should contain (MaximumLengthFailure(ExpectedValid, ExpectedValid.oneOrTwo._path, 2, 3))
      }
      it("Should fail if nested property fails") {
        ExpectedValid.$ops.validate(DObject("noRemoval" ::= ("myb" := ""), "oneOrTwo" ::= ("value" := false))) should contain (NonEmptyOrWhitespaceFailure(ExpectedValid, ExpectedValid.noRemoval.myb._path))
      }
      describe("with deltas") {
        it("Should fail on Null if empty object would fail") {
          ExpectedValid.$ops.validate(DObject("oneOrTwo" := DNull), DObject("oneOrTwo" ::= ("value" := false))) should contain (MinimumLengthFailure(ExpectedValid, ExpectedValid.oneOrTwo._path, 1, 0))
          ExpectedValid.$ops.validate(DObject("noRemoval" := DNull), DObject("noRemoval" ::= ("myb" := "value"))) should contain (KeyRemovalFailure(ExpectedValid, ExpectedValid.noRemoval._path, "myb"))
        }
        it("Should fail on delta if final state fails") {
          ExpectedValid.$ops.validate(DObject("oneOrTwo" ::= ("value3" := 123)), DObject("oneOrTwo" ::= ("value" := false, "value2" := "b"))) should contain (MaximumLengthFailure(ExpectedValid, ExpectedValid.oneOrTwo._path, 2, 3))
          ExpectedValid.$ops.validate(DObject("noRemoval" ::= ("remove" := DNull)), DObject("noRemoval" ::= ("myb" := "value", "remove" := 3))) should contain (KeyRemovalFailure(ExpectedValid, ExpectedValid.noRemoval._path, "remove"))
        }
        it("Should succeeed if delta makes initial bad state, correct") {
          ExpectedValid.$ops.validate(DObject("oneOrTwo" ::= ("value3" := DNull)), DObject("oneOrTwo" ::= ("value" := false, "value2" := 123, "value3" := "vb"))) shouldBe ValidationFailures.empty
        }
      }
    }
  }

  describe("Maybe object validation") {

    describe("Nested object structure") {
      object MaybeEmpty extends Contract {
        val nested = new \\? {}
      }
      object MaybeExpected extends Contract {
        val nested = new \\? {
          val exp = \[String]
        }
      }
      object MaybeMaybe extends Contract {
        val nested = new \\? {
          val myb = \?[String]
        }
      }
      object MaybeClosed extends Contract {
        val nested = new \\? with ClosedFields {
          val myb = \?[String]
        }
      }
      it("Should be valid if object is empty and no expected properties") {
        MaybeEmpty.$ops.validate(DObject.empty) shouldBe ValidationFailures.empty
      }
      it("Should be valid if object is empty and only maybe properties") {
        MaybeMaybe.$ops.validate(DObject.empty) shouldBe ValidationFailures.empty
      }
      it("Should be valid if object is empty and has expected properties") {
        MaybeExpected.$ops.validate(DObject.empty) shouldBe ValidationFailures.empty
      }
      it("Should fail if object is not an object") {
        MaybeExpected.$ops.validate(DObject("nested" := 123)) should contain(IncorrectTypeFailure(MaybeExpected.nested, 123))
      }
      it("Should fail if closed and has extra properties") {
        MaybeClosed.$ops.validate(DObject("nested" ::= ("extra" := 123))) should contain(ClosedContractFailure(MaybeClosed, MaybeClosed.nested._path, "extra"))
      }
      it("Should be valid if closed and contains only included valid properties") {
        MaybeClosed.$ops.validate(DObject("nested" ::= ("myb" := "value"))) shouldBe ValidationFailures.empty
      }
      describe("with deltas") {
        it("Should succeed if nested is null and object has no expected properties") {
          MaybeEmpty.$ops.validate(DObject("nested" := DNull), DObject("nested" ::= ("value" := 123))) shouldBe ValidationFailures.empty
          MaybeMaybe.$ops.validate(DObject("nested" := DNull), DObject("nested" ::= ("myb" := "value"))) shouldBe ValidationFailures.empty
        }
        it("Should succeed if nested is null and object has expected properties") {
          MaybeExpected.$ops.validate(DObject("nested" := DNull), DObject("nested" ::= ("exp" := "value"))) shouldBe ValidationFailures.empty
        }
        it("Should fail if contents fail") {
          MaybeExpected.$ops.validate(DObject("nested" ::= ("exp" := DNull)), DObject("nested" ::= ("exp" := "value"))) should contain(ExpectedFailure(MaybeExpected.nested.exp))
        }
        it("Should succeed if contents succeed") {
          MaybeExpected.$ops.validate(DObject("nested" ::= ("exp" := "value2")), DObject("nested" ::= ("exp" := "value"))) shouldBe ValidationFailures.empty
        }
      }
    }
    describe("Nested object validation") {
      object MaybeValid extends Contract {
        val noRemoval = new \\?(Validators.noKeyRemoval) {
          val myb = \?[String](Validators.nonEmptyOrWhiteSpace)
        }
        val oneOrTwo = new \\?(Validators.minLength(1) && Validators.maxLength(2)) {}
      }
      it("Should succeed if object validation succeeds") {
        MaybeValid.$ops.validate(DObject("noRemoval" ::= ("myb" := "value"), "oneOrTwo" ::= ("value" := false)))
      }
      it("Should succeed if empty expected object would fail and no object value provided") {
        MaybeValid.$ops.validate(DObject.empty) shouldBe ValidationFailures.empty
      }
      it("Should fail if object validation fails") {
        MaybeValid.$ops.validate(DObject("oneOrTwo" ::= ("value" := false, "value2" := 123, "value3" := "v"))) should contain (MaximumLengthFailure(MaybeValid, MaybeValid.oneOrTwo._path, 2, 3))
      }
      it("Should fail if nested property fails") {
        MaybeValid.$ops.validate(DObject("noRemoval" ::= ("myb" := ""), "oneOrTwo" ::= ("value" := false))) should contain (NonEmptyOrWhitespaceFailure(MaybeValid, MaybeValid.noRemoval.myb._path))
      }
      describe("with deltas") {
        it("Should succeed on Null even if empty object would fail") {
          MaybeValid.$ops.validate(DObject("oneOrTwo" := DNull), DObject("oneOrTwo" ::= ("value" := false))) shouldBe ValidationFailures.empty
          MaybeValid.$ops.validate(DObject("noRemoval" := DNull), DObject("noRemoval" ::= ("myb" := "value"))) shouldBe ValidationFailures.empty
        }
        it("Should fail on delta if final state fails") {
          MaybeValid.$ops.validate(DObject("oneOrTwo" ::= ("value3" := 123)), DObject("oneOrTwo" ::= ("value" := false, "value2" := "b"))) should contain (MaximumLengthFailure(MaybeValid, MaybeValid.oneOrTwo._path, 2, 3))
          MaybeValid.$ops.validate(DObject("noRemoval" ::= ("remove" := DNull)), DObject("noRemoval" ::= ("myb" := "value", "remove" := 3))) should contain (KeyRemovalFailure(MaybeValid, MaybeValid.noRemoval._path, "remove"))
        }
        it("Should succeeed if delta makes initial bad state, correct") {
          MaybeValid.$ops.validate(DObject("oneOrTwo" ::= ("value3" := DNull)), DObject("oneOrTwo" ::= ("value" := false, "value2" := 123, "value3" := "vb"))) shouldBe ValidationFailures.empty
        }
      }
    }
  }

  describe("Objects validation") {

  }

  describe("Object map validation") {

  }
  
//
//  test("validation of optional field") {
//    MaybeField.$ops.validate(DObject.empty) shouldBe Vector.empty
//    MaybeField.$ops.validate(DObject("mayNonEmpty" := false)) should be (f(Path("mayNonEmpty") -> "Value is not of the expected type."))
//    MaybeField.$ops.validate(DObject("mayNonEmpty" := "TEST")) shouldBe Vector.empty
//    MaybeField.$ops.validate(DObject("mayNonEmpty" := "")) should be (f(Path("mayNonEmpty") -> "String must not be empty or whitespace."))
//  }
//
//  object DefaultField extends Contract {
//    val inDefault = \![String]("default", Validators.in("default", "one", "two"))
//  }
//
//  test("validation of default field") {
//    DefaultField.$ops.validate(DObject.empty) shouldBe Vector.empty
//    DefaultField.$ops.validate(DObject("inDefault" := false)) should be (f(Path("inDefault") -> "Value is not of the expected type."))
//    DefaultField.$ops.validate(DObject("inDeafult" := "two")) shouldBe Vector.empty
//    DefaultField.$ops.validate(DObject("inDefault" := "three")) should be (f(Path("inDefault") -> "'three' is not an allowed value."))
//  }
//
//  object NestedExpectedField extends Contract {
//
//    val nested = new \\{
//      val expected = \[String]
//    }
//  }
//
//  test("validation of expected nested contract") {
//    NestedExpectedField.$ops.validate(DObject.empty) should be (f(Path("nested") -> "Value is required."))
//    NestedExpectedField.$ops.validate(DObject("nested" := DObject.empty)) should be (f(Path("nested", "expected") -> "Value is required."))
//    NestedExpectedField.$ops.validate(DObject("nested" := DObject("expected" := "value"))) shouldBe Vector.empty
//  }
//
//  object NestedMaybeField extends Contract {
//    val nested = new \\?{
//      val expected = \[String]
//    }
//  }
//
//  test("validation of maybe nested contract") {
//    NestedMaybeField.$ops.validate(DObject.empty) shouldBe Vector.empty
//    NestedMaybeField.$ops.validate(DObject("nested" := DObject.empty)) should be (f(Path("nested", "expected") -> "Value is required."))
//    NestedExpectedField.$ops.validate(DObject("nested" := DObject("expected" := "value"))) shouldBe Vector.empty
//  }
//
//  test("Nested validation") {
//    object NestValid extends Contract {
//      val value1 = \[String]
//      val nest1 = new \\ {
//        val value2 = \[String]
//        val value3 = \[String]
//      }
//      val nest2 = new \\? {
//        val nest3 = new \\ {
//          val value4 = \[String]
//        }
//        val value5 = \[String]
//      }
//    }
//
//    val json1 = DObject("value1" := "V", "nest1" := DObject("value2" := "V", "value3" := "V"))
//    NestValid.$ops.validate(json1) shouldBe Vector.empty
//    val json2 = DObject("value1" := "V", "nest1" := DObject("value2" := "V", "value3" := "V"), "nest2" := DObject("nest3" := DObject("value4" := "V"), "value5" := "V"))
//    NestValid.$ops.validate(json2) shouldBe Vector.empty
//
//    NestValid.$ops.validate(DObject("value1" := "V", "nest1" := DObject("value3" := 3))).toSet shouldBe Set("nest1"\"value2" -> "Value is required.", "nest1"\"value3" -> "Value is not of the expected type.")
//
//    NestValid.$ops.validate(DObject("value1" := "V", "nest2" := DObject.empty)).toSet shouldBe Set(Path("nest1") -> "Value is required." , "nest2"\"nest3" -> "Value is required.", "nest2"\"value5" -> "Value is required.")
//  }
//
//  object ContractArray extends Contract {
//    val array = \::(ExpectedField)
//  }
//
//  test("Contract array validation") {
//    ContractArray.$ops.validate(DObject.empty) shouldBe Vector.empty
//    ContractArray.$ops.validate(DObject("array" -> DArray.empty)) shouldBe Vector.empty
//
//    ContractArray.$ops.validate(DObject("array" -> DArray(DObject("expGT" := 6)))) shouldBe Vector.empty
//    ContractArray.$ops.validate(DObject("array" -> DArray(DObject("expGT" := 6), DObject("expGT" := 8)))) shouldBe Vector.empty
//    ContractArray.$ops.validate(DObject("array" -> DArray(DObject("expGT" := 4)))) should be (f("array" \ 0 \ "expGT" -> "Value 4 is not greater than 5."))
//    ContractArray.$ops.validate(DObject("array" -> DArray(DObject("expGT" := 6), DObject("expGT" := 4)))) should be (f("array" \ 1 \ "expGT" -> "Value 4 is not greater than 5."))
//  }
//
//  object ContractArrayNonEmpty extends Contract {
//    val array  = \::(ExpectedField, Validators.nonEmpty)
//  }
//
//  test("Contract array nonEmpty validation") {
//    ContractArrayNonEmpty.$ops.validate(DObject.empty) shouldBe f(Path("array") -> "Value must not be empty.")
//    ContractArrayNonEmpty.$ops.validate(DObject("array" -> DArray.empty)) shouldBe f(Path("array") -> "Value must not be empty.")
//    ContractArrayNonEmpty.$ops.validate(DObject("array" -> DArray(DObject("expGT" := 6)))) shouldBe Vector.empty
//  }
//
//
//  object Element extends Contract {
//    val id = \[Int]
//    val name = \?[String]
//  }
//
//  object Parent extends Contract {
//    val elements = \->[String, DObject](Element)
//  }
//
//  test("map contract validator") {
//
//    Parent.$ops.validate(DObject.empty) shouldBe Vector.empty
//
//    val succ = DObject("elements" := Map("first" -> DObject("id" := 4), "second" -> DObject("id" := 2, "name" := "bob")))
//    Parent.$ops.validate(succ) shouldBe Vector.empty
//
//    val failfirst = DObject("elements" := Map("first" -> DObject(), "second" -> DObject("id" := 2, "name" := "bob")))
//    Parent.$ops.validate(failfirst) should be (f(Path("elements", "first", "id") -> "Value is required."))
//
//    val failSecond = DObject("elements" := Map("first" -> DObject("id" := 4), "second" -> DObject("id" := 2, "name" := false)))
//    Parent.$ops.validate(failSecond) should be (f(Path("elements", "second", "name") -> "Value is not of the expected type."))
//
//    val failBoth = DObject("elements" := Map("first" -> DObject(), "second" -> DObject("id" := 2, "name" := false)))
//    Parent.$ops.validate(failBoth) should be (f(Path("elements", "first", "id") -> "Value is required.",Path("elements", "second", "name") -> "Value is not of the expected type."))
//  }
//
//  test("delta validation") {
//    val current = Element.$create(e => e.id.$set(1) ~ e.name.$set("Value"))
//    //val delta = Element.$create(_.name.$setNull)
//    //Element.$ops.validate(delta, current) shouldBe Vector.empty
//  }
//
//  object Nulls extends Contract {
//    val int = \?[Int]
//    val bool = \[Boolean]
//
//    val nested = new \\? {}
//  }
//
//
//  test("Null validation") {
//    val all = DObject("int" := 10, "bool" := true, "nested" -> DObject("values" := 1))
//    Nulls.$ops.validate(DObject("int" := DNull), all) shouldBe Vector.empty
//    Nulls.$ops.validate(DObject("nested" := DNull), all) shouldBe Vector.empty
//    Nulls.$ops.validate(DObject("nested" := ("values" := DNull)), all) shouldBe Vector.empty
//    Nulls.$ops.validate(DObject("bool" := DNull), all) shouldBe f(Path("bool") -> "Value is required.")
//  }
//
//  object Keys extends Contract {
//    val nested = new \\?(Validators.keyValidator("[a-z]*".r, "Invalid key"))
//  }
//
//
//  test("key Validation") {
//    Keys.$ops.validate(DObject("nested" -> DObject("values" := 1))) shouldBe Vector.empty
//    Keys.$ops.validate(DObject("nested" -> DObject("123" := 3))) shouldBe f(Path("nested", "123") -> "Invalid key")
//  }
//
//  object Appending extends Contract {
//    val map = \?[Map[String, String]](Validators.noKeyRemoval)
//  }
//
//  test("No key removal only") {
//    val obj = DObject("map" := Map("Key" := "value") )
//    Appending.$ops.validate(DObject("map" := ("Key" := "Value2")), obj) shouldBe Vector.empty
//    Appending.$ops.validate(DObject("map" := ("Key2" := "Value2")), obj) shouldBe Vector.empty
//    Appending.$ops.validate(DObject("map" := ("Key" := DNull)), obj) should not be Vector.empty
//    Appending.$ops.validate(DObject("map" := ("Key2" := DNull)), obj) shouldBe Vector.empty
//  }
//
//  object Closed extends Contract with ClosedFields {
//    val internalClosed = new \\? with ClosedFields {
//      val one = \[String]
//      val two = \?[Boolean]
//    }
//    val internalOpen = new \\? {
//      val one = \[String]
//    }
//  }
//
//  test("Closing an objects key options") {
//    Closed.$ops.validate(DObject.empty) shouldBe Vector.empty
//    Closed.$ops.validate(DObject("unexpected" := 2)) shouldBe f(Path.empty -> "Additional key 'unexpected' not allowed.")
//    Closed.$ops.validate(DObject("internalClosed" ::= ("one" := "value"))) shouldBe Vector.empty
//    Closed.$ops.validate(DObject("internalOpen" ::= ("one" := "value", "three" := 3))) shouldBe Vector.empty
//    Closed.$ops.validate(DObject("internalClosed" ::= ("one" := "value", "three" := 3))) shouldBe f(Path("internalClosed") -> "Additional key 'three' not allowed.")
//  }
//
//  object ReservedAndInternal extends Contract {
//    val internalP = \?[String](Validators.internal)
//
//    val reservedP = \?[Boolean](Validators.reserved)
//  }
//
//  test("Reserved and internal") {
//    ReservedAndInternal.$ops.validate(DObject.empty) shouldBe Vector.empty
//    ReservedAndInternal.$ops.validate(DObject("internalP" := "internal")) shouldBe f(Path("internalP") -> "Value is reserved and cannot be provided.")
//    ReservedAndInternal.$ops.validate(DObject("reservedP" := false)) shouldBe f(Path("reservedP") -> "Value is reserved and cannot be provided.")
//  }

}
