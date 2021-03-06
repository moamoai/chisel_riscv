/*
 *
 * An ALU is a minimal start for a processor.
 *
 */

package riscv

import chisel3._
import chisel3.util._
// import chisel3.util.experimental.loadMemoryFromFile
// import chisel3.util.experimental.MemoryLoadFileType

class Memory_BD extends Module{
  val io = IO(new Bundle {
    val if_mem_bd = new IF_MEM_BD
    val we        = Input (UInt(1.W))
    val wdata     = Input (UInt(32.W))
    val addr      = Input (UInt(16.W))
    val addr2     = Input (UInt(16.W))
    val rdata     = Output(UInt(32.W))
    val rdata2    = Output(UInt(32.W))
  })

  val i_mem = Module(new Memory)
  val we    = Wire(UInt(1.W))
  val wdata = Wire(UInt(32.W))
  val addr  = Wire(UInt(16.W))
  we    := 0.U
  wdata := 0.U
  addr  := 0.U

  i_mem.io.wdata := wdata
  i_mem.io.we    := we
  i_mem.io.addr  := addr
  i_mem.io.addr2 := io.addr2

  io.rdata       := i_mem.io.rdata
  io.rdata2      := i_mem.io.rdata2
  io.if_mem_bd.rdata := i_mem.io.rdata

  when(io.if_mem_bd.bd_en === 1.U){
    addr  := io.if_mem_bd.addr
    we    := io.if_mem_bd.we
    wdata := io.if_mem_bd.wdata
  }.otherwise{
    addr  := io.addr
    we    := io.we
    wdata := io.wdata
  }
}

class Memory extends Module {
  val io = IO(new Bundle {
    val we     = Input (UInt(1.W))
    val wdata  = Input (UInt(32.W))
    val addr   = Input (UInt(16.W))
    val addr2  = Input (UInt(16.W))
    val rdata  = Output(UInt(32.W))
    val rdata2 = Output(UInt(32.W))
  })

  // Use shorter variable names
  val we    = io.we
  val wdata = io.wdata
  val addr  = io.addr
  val addr2 = io.addr2

  val rdata  = Wire(UInt(32.W))
  val rdata2 = Wire(UInt(32.W))
  // some default value is needed
  rdata := 0.U

  val my_mem = Mem((1<<16), UInt(32.W))

  //loadMemoryFromFile(my_mem, "mem1.txt")
  // The ALU selection
  when(we === 1.U) {
    my_mem(addr) := wdata
  }
  rdata  := my_mem(addr)
  rdata2 := my_mem(addr2)

  io.rdata  := rdata
  io.rdata2 := rdata2
}

// Generate the Verilog code by invoking the Driver
object MemoryMain extends App {
  println("Generating the Memory hardware")
  chisel3.Driver.execute(Array("--target-dir", "generated"), () => new Memory())
}

