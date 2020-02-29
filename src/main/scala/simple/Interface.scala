package simple

import chisel3._
import chisel3.util._

class IF_MEM_BD  extends Bundle {
    val bd_en     = Input (UInt(1.W))
    val we        = Input (UInt(1.W))
    val wdata     = Input (UInt(32.W))
    val addr      = Input (UInt(16.W))
    val rdata     = Output(UInt(32.W))
}

class IF_IFtoID  extends Bundle {
  val opcode     = Output(UInt(32.W))
  val valid      = Output(UInt( 1.W))
  val PC         = Output(UInt(32.W))
}

class IF_IDtoIF  extends Bundle {
  val jump_addr  = Output(UInt(32.W))
  val jump_valid = Output(UInt( 1.W))
}

class IF_RFtoID  extends Bundle {
  val d_rs1 = Output(UInt(32.W))
  val d_rs2 = Output(UInt(32.W))
}

class IF_IDtoRF  extends Bundle {
  val rd         = Output(UInt( 5.W))
  val rs1        = Output(UInt( 5.W))
  val rs2        = Output(UInt( 5.W))
  // val valid      = Output(UInt( 1.W)) // not need
}
class IF_IDtoEX  extends Bundle {
  val alu_func    = Output(UInt( 6.W))
  val ldst_func   = Output(UInt( 6.W))
  val imm         = Output(UInt(32.W))
  val imm_sel     = Output(UInt( 1.W))
  val rd          = Output(UInt( 5.W))
  val load_valid  = Output(UInt( 1.W))
  val alu_valid   = Output(UInt( 1.W))
  val store_valid = Output(UInt( 1.W))
}
class IF_RFtoEX  extends Bundle {
  val d_rd       = Output(UInt(32.W))
  val d_rs1      = Output(UInt(32.W))
  val d_rs2      = Output(UInt(32.W))
//  val valid      = Output(UInt( 1.W)) // not need
}
class IF_EXtoWB  extends Bundle {
  val rd         = Output(UInt( 5.W))
  val wbdata     = Output(UInt(32.W))
  val wbvalid    = Output(UInt( 1.W))
  val valid      = Output(UInt( 1.W))
}
class IF_WBtoRF  extends Bundle {
  val rd         = Output(UInt( 5.W))
  val wdata      = Output(UInt(32.W))
  val valid      = Output(UInt( 1.W))
}