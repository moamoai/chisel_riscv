package simple

import chisel3._
import chisel3.util._

import chisel3.util.experimental.loadMemoryFromFile

class RiscV extends Module {
  val io = IO(new Bundle {
    val start         = Input (UInt(32.W))
    val inst_code     = Input (UInt(32.W))
    val inst_valid    = Input (UInt(1.W))
    val inst_addr     = Output (UInt(32.W))
    val inst_ready    = Output(UInt(1.W))
    val error         = Output(UInt( 1.W))

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
  i_if.io.start      := io.start
  i_if.io.ready      := i_wb.io.ready
  i_if.io.inst_code  := code
  i_if.io.inst_valid := valid
  i_id.io.if_IFtoID  := i_if.io.if_IFtoID
  io.inst_addr       := i_if.io.inst_addr

  // ID Stage
  i_ex.io.if_IDtoEX := i_id.io.if_IDtoEX
  i_rf.io.if_IDtoRF := i_id.io.if_IDtoRF

  // EX stage
  i_ex.io.if_RFtoEX := i_rf.io.if_RFtoEX
  i_wb.io.if_EXtoWB := i_ex.io.if_EXtoWB

  // WB stage
  i_rf.io.if_WBtoRF := i_wb.io.if_WBtoRF

  // Error
  val i_err = Module(new ErrorM)
  i_err.io.illigal_op := i_id.io.illigal_op
  io.error := i_err.io.error


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

