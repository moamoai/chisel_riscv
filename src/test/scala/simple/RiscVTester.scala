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
          exp       : Int
    ) : Int = {
    f_run_instruction(inst_code)
    expect(dut.io.info_rf(reg_num), exp)
    var reg_val = peek(dut.io.info_rf(reg_num))
    println(f"reg_num[0x$reg_num%02x] reg_val[0x$reg_val%08x] exp[0x$exp%08x]");
    return 0;
  }
  def f_run_instruction(
          inst_code : UInt
    ) : Int = {
    poke(dut.io.inst_valid, 1.U)
    poke(dut.io.inst_code , inst_code)
    step(1)

    var ready = peek(dut.io.inst_ready)
    while (ready == 0){
      step(1)
      ready = peek(dut.io.inst_ready)
    }
    poke(dut.io.inst_valid, 0.U)
    step(1)
    return 0;
  }
  val filename = "inst.txt"
  for (line <- Source.fromFile(filename).getLines) {
      println(line)
      // println(lines(0))
      // println(lines(1))
      // println(lines(2))
      var lines = line.split(" ")
      var inst_code = lines(0)
      var reg_num   = Integer.parseInt(lines(1), 16)
      var expect    = Integer.parseInt(lines(2), 16)
      
      var inst = Integer.parseInt(inst_code,16).U
      // f_run_instruction(inst)
      f_run_instruction_exp(inst, reg_num, expect)
      step(1)
  }


}

object RiscVTester extends App {
  println("Testing the ALU")
  iotesters.Driver.execute(Array[String]("--generate-vcd-output", "on"), () => new RiscV()) {
    c => new RiscVTester(c)
  }
}
