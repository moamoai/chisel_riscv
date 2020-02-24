package simple

import chisel3._
import chisel3.iotesters.PeekPokeTester

/**
 * Test the RiscV design
 */
class RiscVTester(dut: RiscV) extends PeekPokeTester(dut) {
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

  f_run_instruction(OBJ_OPCODE.OP_Nop)
  step(1)

}

object RiscVTester extends App {
  println("Testing the ALU")
  iotesters.Driver.execute(Array[String]("--generate-vcd-output", "on"), () => new RiscV()) {
    c => new RiscVTester(c)
  }
}
