package simple

import chisel3._
import chisel3.util._

object OBJ_ALU_FUNC {
  val ADD  = 0.U(4.W)
  val SUB  = 1.U(4.W)
//  val   = 2.U(4.W)
//  val   = 3.U(4.W)
  val XOR  = 4.U(4.W)
//  val   = 5.U(4.W)
  val OR  = 6.U(4.W)
  val AND  = 7.U(4.W)
  val SEL_A  = 8.U(4.W)
  val SEL_B  = 9.U(4.W)
}

object REG {
  val RD_0  = 0.U(5.W)
  val RD_1  = 1.U(5.W)
  val RD_2  = 2.U(5.W)
  val RD_3  = 3.U(5.W)
  val RD_4  = 4.U(5.W)
  val RD_5  = 5.U(5.W)
  val RD_6  = 6.U(5.W)
  val RD_7  = 7.U(5.W)
  val RD_8  = 8.U(5.W)

  val RS1_0  = 0.U(5.W)
  val RS1_1  = 1.U(5.W)
  val RS1_2  = 2.U(5.W)
  val RS1_3  = 3.U(5.W)
  val RS1_4  = 4.U(5.W)
  val RS1_5  = 5.U(5.W)
  val RS1_6  = 6.U(5.W)
  val RS1_7  = 7.U(5.W)
  val RS1_8  = 8.U(5.W)

  val RS2_0  = 0.U(5.W)
  val RS2_1  = 1.U(5.W)
  val RS2_2  = 2.U(5.W)
  val RS2_3  = 3.U(5.W)
  val RS2_4  = 4.U(5.W)
  val RS2_5  = 5.U(5.W)
  val RS2_6  = 6.U(5.W)
  val RS2_7  = 7.U(5.W)
  val RS2_8  = 8.U(5.W)
}