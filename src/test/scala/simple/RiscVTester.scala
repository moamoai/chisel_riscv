package simple

import chisel3._
import chisel3.iotesters.PeekPokeTester

import scala.io.Source

/**
 * Test the RiscV design
 */
class RiscVTester(dut: RiscV) extends PeekPokeTester(dut) {
  def f_run_instruction_exp(
          inst_code : UInt,
          reg_num   : Int,
          exp       : BigInt
    ) : Int = {
    var result = 1 // 1:OK 0:NG
    result &= f_run_instruction(inst_code)
    expect(dut.io.info_rf(reg_num), exp)
    var reg_val = peek(dut.io.info_rf(reg_num))
    println(f"reg_num[0x$reg_num%02x] reg_val[0x$reg_val%08x] exp[0x$exp%08x]");
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

  // Start Sim
  poke(dut.io.start, 1.U)
  step(1)
  poke(dut.io.start, 0.U)
  step(1)
  val filename = "inst.txt"
  var line_list = Source.fromFile(filename).getLines.toList
  // for (line <- line_list) {
  var inst_addr = 0
  var timer     = 10 // 10 cycle

  while((timer>0)){
    inst_addr = peek(dut.io.inst_addr).intValue()
    var line = line_list(inst_addr)
    println(line)
    var lines = line.split(" ")
    var inst_code = lines(0)
    var inst      = BigInt(inst_code, 16)
    // var reg_num   = BigInt(lines(1), 16)
    var expect    = BigInt(lines(2), 16)
    var EXP_ADDR  = BigInt(lines(4), 16) // Next PC

    // var inst      = Integer.parseInt(inst_code,16).U
    var reg_num   = Integer.parseInt(lines(1), 16)
    // var expect    = Integer.parseInt(lines(2), 16)
    // var EXP_ADDR  = Integer.parseInt(lines(3), 16) // cuurent PC
    // var EXP_ADDR  = Integer.parseInt(lines(4), 16) // Next PC

    // f_run_instruction(inst)
    if(f_run_instruction_exp(inst.U, reg_num, expect)!=1){
      println("[NG] f_run_instruction_exp")
      timer = 0
    }
    step(1)

    // Next ADDR Check
    inst_addr = peek(dut.io.inst_addr).intValue()
    if((inst_addr*4)!=EXP_ADDR){
       println(f"[NG] inst_addr[0x$inst_addr%08x] EXP[0x$EXP_ADDR%08x]");
       timer = 0
    }

    timer   = timer -1
  }


}

object RiscVTester extends App {
  println("Testing the ALU")
  iotesters.Driver.execute(Array[String]("--generate-vcd-output", "on"), () => new RiscV()) {
    c => new RiscVTester(c)
  }
}
