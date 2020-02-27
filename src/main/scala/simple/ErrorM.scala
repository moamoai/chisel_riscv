package simple

import chisel3._
import chisel3.util._

class ErrorM extends Module {
  val io = IO(new Bundle {
    val illigal_op = Input (UInt( 1.W))
    val error      = Output(UInt( 1.W))
  })
  io.error := io.illigal_op
}