package nand2tetris.vmt.translator

import nand2tetris.vmt.parser.Stack._

import nand2tetris.vmt.translator.TranslationHelper.Stack._
import nand2tetris.vmt.translator.TranslationHelper.Register._
import nand2tetris.vmt.translator.TranslationHelper.Segment._
import nand2tetris.vmt.translator.TranslationHelper._
import nand2tetris.vmt.translator.CompPreamble._

object CodeWriter {
  def translate(stack: Stack): Seq[String] =
    compPreamble(stack) ++ stack.zipWithIndex.flatMap {
      case (line, i) => translate(line, i)
    }

  def translate(line: StackLine, i: Int): Seq[String] = line match {
    case Op(op) => translateLogic(op, i)
    case Memory(action, segment, j) => translateSegment(action, segment, j)
    case Fluff => Nil
  }

  def translateLogic(line: StackOps, i: Int): Seq[String] = line match {
    case Unitary(op) => mutate(op)
    case Arithmetic(op) => pop ++ setReg(0) ++ pop ++ mutateReg(0, "D" + op + "M") ++ getReg(0) ++ push
    case Compare(jmp) => pop ++ setReg(0) ++ pop ++ mutateReg(0, "D-M") ++ comp(jmp, i.toString) ++ push
  }

  def translateSegment(pushAction: Boolean, segment: Segment, i: Int): Seq[String] =
    (segment, pushAction) match {
      case (Relative(pointer), false) => pop ++ setToSeg(pointer, i)
      case (Relative(pointer), true) => getFromSeg(pointer, i) ++ push
      case (Constant, true) => constant(i) ++ push
      case (Absolute(f), false) => pop ++ setTo(f(i))
      case (Absolute(f), true) => getFrom(f(i)) ++ push
    }
}