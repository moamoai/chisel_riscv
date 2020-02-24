package simple

import chisel3._
import chisel3.util._

import chisel3.util.experimental.loadMemoryFromFile
class IF extends Module {
  val io = IO(new Bundle {
    val inst_code      = Input (UInt(32.W))
    val inst_valid     = Input (UInt(1.W))
    val if_IFtoID      = new IF_IFtoID
  })

  // Output
  io.if_IFtoID.opcode :=  io.inst_code
  io.if_IFtoID.valid  :=  io.inst_valid
}

class ID extends Module {
  val io = IO(new Bundle {
    val if_IFtoID      = Flipped(new IF_IFtoID)
    var if_IDtoEX      = new IF_IDtoEX
    var if_IDtoRF      = new IF_IDtoRF
  })

  // Output
  io.if_IDtoEX.alu_func  := 1.U
  io.if_IDtoEX.ldst_func := 2.U
  io.if_IDtoEX.imm       := 3.U
  io.if_IDtoEX.rd        := 4.U
  io.if_IDtoEX.valid     :=  io.if_IFtoID.valid

  io.if_IDtoRF.rd        := 5.U
  io.if_IDtoRF.rs1       := 6.U
  io.if_IDtoRF.rs2       := 7.U
}

class EX extends Module {
  val io = IO(new Bundle {
    var if_IDtoEX = Flipped(new IF_IDtoEX)
    var if_RFtoEX = Flipped(new IF_RFtoEX)
    var if_EXtoWB = new IF_EXtoWB
  })
  io.if_EXtoWB.rd    := 0.U
  io.if_EXtoWB.d_alu := 0.U
  io.if_EXtoWB.d_ld  := 0.U
  io.if_EXtoWB.valid :=  io.if_IDtoEX.valid
}

class RF(DEBUG_INFO_EN:Int = 1) extends Module {
  val io = IO(new Bundle {
    var if_RFtoEX = new IF_RFtoEX
    var if_WBtoRF = Flipped(new IF_WBtoRF)
    var if_IDtoRF = Flipped(new IF_IDtoRF)
    // Debug Port
    // var info_rf = if(DEBUG_INFO_EN==1) Output(Vec(4, UInt(32.W))) else None
    var info_rf = Output(Vec(32, UInt(32.W)))
  })
  val r_RegFile = RegInit(VecInit(Seq.fill(32)(0.U(32.W))))

  io.info_rf := r_RegFile
//  val info_rf = Wire(Vec(32, UInt(32.W)))
//  if(DEBUG_INFO_EN==1){
//    info_rf := r_RegFile
//    io.info_rf := info_rf
//  }

  var rd     = io.if_IDtoRF.rd
  var rs1    = io.if_IDtoRF.rs1
  var rs2    = io.if_IDtoRF.rs2

  var wvalid = io.if_WBtoRF.valid
  var wdata  = io.if_WBtoRF.wdata
  var w_rd   = io.if_WBtoRF.rd

  io.if_RFtoEX.d_rd  := r_RegFile(rd )
  io.if_RFtoEX.d_rs1 := r_RegFile(rs1)
  io.if_RFtoEX.d_rs2 := r_RegFile(rs2)

  when(wvalid===1.U){
    r_RegFile(w_rd) := wdata
  }
}

class WB extends Module {
  val io = IO(new Bundle {
    var if_EXtoWB = Flipped(new IF_EXtoWB)
    var if_WBtoRF = new IF_WBtoRF
    val ready     = Output(UInt( 1.W))
  })
  io.if_WBtoRF.rd    := 1.U
  io.if_WBtoRF.wdata := 0x1234.U
  io.if_WBtoRF.valid :=  io.if_EXtoWB.valid

  io.ready           :=  io.if_EXtoWB.valid
}

class RiscV extends Module {
  val io = IO(new Bundle {
    val inst_code     = Input (UInt(32.W))
    val inst_valid    = Input (UInt(1.W))
    val inst_ready     = Output(UInt(1.W))

    // Debug Port
    var info_rf = Output(Vec(32, UInt(32.W)))
  })

  // Use shorter variable names
  val valid         = io.inst_valid
  val code          = io.inst_code

  // Instance
  val i_if = Module(new IF)
  val i_id = Module(new ID)
  val i_rf = Module(new RF)
  val i_ex = Module(new EX)
  val i_wb = Module(new WB)

  // IF stage
  i_if.io.inst_code  := code
  i_if.io.inst_valid := valid
  i_id.io.if_IFtoID  := i_if.io.if_IFtoID

  // ID Stage
  i_ex.io.if_IDtoEX := i_id.io.if_IDtoEX
  i_rf.io.if_IDtoRF := i_id.io.if_IDtoRF

  // EX stage
  i_ex.io.if_RFtoEX := i_rf.io.if_RFtoEX
  i_wb.io.if_EXtoWB := i_ex.io.if_EXtoWB

  // WB stage
  i_rf.io.if_WBtoRF := i_wb.io.if_WBtoRF

  // Output
  io.inst_ready  := i_wb.io.ready

  // Debug Output
  io.info_rf := i_rf.io.info_rf
}

// Generate the Verilog code by invoking the Driver
object RiscVMain extends App {
  println("Generating the RiscV hardware")
  chisel3.Driver.execute(Array("--target-dir", "generated"), () => new RiscV())
}

