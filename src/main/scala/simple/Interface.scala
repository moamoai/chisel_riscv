package simple

import chisel3._
import chisel3.util._

class IF_IFtoID  extends Bundle {
  val opcode     = Output(UInt(32.W))
  val valid      = Output(UInt( 1.W))
}
class IF_IDtoRF  extends Bundle {
  val rd         = Output(UInt( 5.W))
  val rs1        = Output(UInt( 5.W))
  val rs2        = Output(UInt( 5.W))
  // val valid      = Output(UInt( 1.W)) // not need
}
class IF_IDtoEX  extends Bundle {
  val alu_func   = Output(UInt( 6.W))
  val ldst_func  = Output(UInt( 6.W))
  val imm        = Output(UInt(32.W))
  val imm_sel    = Output(UInt( 1.W))
  val rd         = Output(UInt( 5.W))
  val valid      = Output(UInt( 1.W))
}
class IF_RFtoEX  extends Bundle {
  val d_rd       = Output(UInt(32.W))
  val d_rs1      = Output(UInt(32.W))
  val d_rs2      = Output(UInt(32.W))
//  val valid      = Output(UInt( 1.W)) // not need
}
class IF_EXtoWB  extends Bundle {
  val rd         = Output(UInt( 5.W))
  val d_alu      = Output(UInt(32.W))
  val d_ld       = Output(UInt(32.W))
  val valid      = Output(UInt( 1.W))
}
class IF_WBtoRF  extends Bundle {
  val rd         = Output(UInt( 5.W))
  val wdata      = Output(UInt(32.W))
  val valid      = Output(UInt( 1.W))
}