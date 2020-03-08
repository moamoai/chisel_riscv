package riscv

import chisel3._
import chisel3.util._

class WB extends Module {
  val io = IO(new Bundle {
    var if_EXtoWB = Flipped(new IF_EXtoWB)
    var if_WBtoRF = new IF_WBtoRF
    val ready     = Output(UInt( 1.W))
  })
  io.if_WBtoRF.rd    := io.if_EXtoWB.rd
  io.if_WBtoRF.wdata := io.if_EXtoWB.wbdata
  io.if_WBtoRF.valid := io.if_EXtoWB.wbvalid
  io.ready           := io.if_EXtoWB.valid
}