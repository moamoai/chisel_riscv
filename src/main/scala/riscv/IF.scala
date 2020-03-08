package riscv

import chisel3._
import chisel3.util._

class IF extends Module {
  val io = IO(new Bundle {
    val start          = Input (UInt(1.W))
    val inst_code      = Input (UInt(32.W))
    val inst_valid     = Input (UInt(1.W))
    val ready          = Input (UInt(1.W))
    val if_IFtoID      = new IF_IFtoID
    val if_IDtoIF      = Flipped(new IF_IDtoIF)
    val inst_addr      = Output (UInt(32.W))
    val inst_ready     = Output (UInt(1.W))
  })
  val r_PC    = RegInit("h8000_0000".U(32.W))
  val r_ready = RegInit(0.U(1.W))
  val ready   = io.ready
  io.inst_addr := r_PC
  when(io.start===1.U){
      r_ready := 1.U
  }.elsewhen(ready===1.U){
      r_ready := 1.U
  }.otherwise{
      r_ready := 0.U
  }
  io.inst_ready := r_ready
  when(io.if_IDtoIF.jump_valid===1.U){
    r_PC := io.if_IDtoIF.jump_addr
  }.elsewhen(ready===1.U){
    r_PC := r_PC + 4.U
  }

  // Output
  io.if_IFtoID.opcode :=  io.inst_code
  io.if_IFtoID.valid  :=  io.inst_valid
  io.if_IFtoID.PC     :=  r_PC
}