package simple

import chisel3._
import chisel3.iotesters.PeekPokeTester

import scala.io.Source

/**
 * Test the RiscV design
 */
class RiscVTester(dut: RiscV) extends PeekPokeTester(dut) {
  def mem_write(
    addr : UInt,
    wdata: UInt
  ): Int ={
    poke(dut.io.if_mem_bd.bd_en, 1.U)
    poke(dut.io.if_mem_bd.we   , 1.U)
    poke(dut.io.if_mem_bd.addr , addr)
    poke(dut.io.if_mem_bd.wdata, wdata)
    step(1)
    poke(dut.io.if_mem_bd.bd_en, 0.U)
    poke(dut.io.if_mem_bd.we   , 0.U)
    return 0;
  }
  def mem_read(
    addr : UInt
  ): BigInt ={
    poke(dut.io.if_mem_bd.bd_en, 1.U)
    poke(dut.io.if_mem_bd.addr , addr)
    var rdata = peek(dut.io.if_mem_bd.rdata)
    step(1)
    poke(dut.io.if_mem_bd.bd_en, 0.U)
    println(f"addr[0x$addr%04x] rdata[0x$rdata%08x]");
    return rdata;
  }
  def f_run_instruction_exp(
          inst_code : UInt,
          reg_num   : Int,
          exp       : BigInt
    ) : Int = {
    var result = 1 // 1:OK 0:NG
    result &= f_run_instruction(inst_code)
    expect(dut.io.info_rf(reg_num), exp)
    var reg_val = peek(dut.io.info_rf(reg_num))
    if(reg_val != exp){
      println(f"reg_num[0x$reg_num%02x] reg_val[0x$reg_val%08x] exp[0x$exp%08x]");
      result = 0
    }
    return result;
  }
  def f_run_instruction(
          inst_code : UInt
    ) : Int = {
    var result = 1 // 1:OK 0:NG
    poke(dut.io.inst_valid, 1.U)
    poke(dut.io.inst_code , inst_code)
    step(1)

    var ready = peek(dut.io.inst_ready)
    var error = peek(dut.io.error)
    if(error == 1){
      println("[NG] Error Assert")
      result = 0
    }else{
      while (ready == 0){
        step(1)
        ready = peek(dut.io.inst_ready)
      }
    }
    poke(dut.io.inst_valid, 0.U)
    step(1)
    return result;
  }

  // Init Mem
  var data_list = Source.fromFile("data.txt").getLines.toList
  for (data <- data_list){
    var tmp = data.split(" ")
    var ADDR = BigInt(tmp(0),16) & 0x0000FFFFL
    var DATA = BigInt(tmp(1),16)
    mem_write(ADDR.U, DATA.U)
    mem_read (ADDR.U)
  }

  // Start Sim
  poke(dut.io.start, 1.U)
  step(1)
  poke(dut.io.start, 0.U)
  step(1)

  var inst_list = Source.fromFile("inst.txt").getLines.toList
  var inst_map: Map[BigInt, BigInt] = Map.empty
  for (inst <- inst_list){
    var tmp = inst.split(" ")
    var ADDR = BigInt(tmp(0),16)
    var CODE = BigInt(tmp(1),16)
    inst_map = inst_map + (ADDR -> CODE)
  }

  val filename = "expect.txt"
  var line_list = Source.fromFile(filename).getLines.toList
  // for (line <- line_list) {
  var inst_addr = BigInt(0)
  var base_addr = BigInt(0x80000000)
  var TIME_MAX  = 1000
  var timer     = 0 // 10 cycle

  while((TIME_MAX > timer)){
    inst_addr = peek(dut.io.inst_addr) // .intValue()
    var inst  = inst_map(inst_addr)

    // println(f"inst_addr[0x$inst_addr%08x]")
    var line = line_list(timer) // line_addr/4
    var lines = line.split(" ")
    // var inst_code = lines(0)
    // var inst      = BigInt(inst_code, 16)
    // var reg_num   = BigInt(lines(1), 16)
    var expect    = BigInt(lines(2), 16)
    var EXP_ADDR  = BigInt(lines(4), 16) // Next PC

    // var inst      = Integer.parseInt(inst_code,16).U
    var reg_num   = Integer.parseInt(lines(1), 10)
    // var expect    = Integer.parseInt(lines(2), 16)
    // var EXP_ADDR  = Integer.parseInt(lines(3), 16) // cuurent PC
    // var EXP_ADDR  = Integer.parseInt(lines(4), 16) // Next PC

    // f_run_instruction(inst)
    if(inst == 0x73){ // ECALL instruction.
      println(f"################################################################");
      println(f"[TMP_OK(ECALL)] inst_addr[0x$inst_addr%08x] EXP[0x$EXP_ADDR%08x]");
      println(f"################################################################");
      timer = TIME_MAX
    }else{
      if(f_run_instruction_exp(inst.U, reg_num, expect)!=1){
        println(f"[NG] f_run_instruction_exp")
        println(f"[NG] ADDR[0x$inst_addr%08x]: CODE[0x$inst%08x]")
         println("[NG] ERROR LINE: "+line)
        timer = TIME_MAX
      }
      step(1)
      // Next ADDR Check
      inst_addr = peek(dut.io.inst_addr)
      if(EXP_ADDR!=BigInt(0xbeef)){ // Last instruction
        if(inst_addr!=EXP_ADDR){
           println(f"[NG] inst_addr[0x$inst_addr%08x] EXP[0x$EXP_ADDR%08x]");
           println("ERROR LINE: "+line)
           timer = TIME_MAX
        }
      }else{
        println(f"################################################################");
        println(f"[OK] COMPLETE!! inst_addr[0x$inst_addr%08x] EXP[0x$EXP_ADDR%08x]");
        println(f"################################################################");
        timer = TIME_MAX
      }
    }

    timer   = timer + 1
  }


}

object RiscVTester extends App {
  println("Testing the ALU")
  iotesters.Driver.execute(Array[String]("--generate-vcd-output", "on"), () => new RiscV()) {
    c => new RiscVTester(c)
  }
}
