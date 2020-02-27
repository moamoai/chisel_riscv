package simple

import chisel3._
import chisel3.util._

import chisel3.util.experimental.loadMemoryFromFile

class EX extends Module {
  val io = IO(new Bundle {
    var if_IDtoEX = Flipped(new IF_IDtoEX)
    var if_RFtoEX = Flipped(new IF_RFtoEX)
    var if_EXtoWB = new IF_EXtoWB
  })

  var alu_func = io.if_IDtoEX.alu_func
  var imm_sel  = io.if_IDtoEX.imm_sel
  val alu_a    = Wire(UInt(32.W))
  val alu_b    = Wire(UInt(32.W))
  val o_alu    = Wire(UInt(32.W))
  alu_a := io.if_RFtoEX.d_rs1
  when(imm_sel === 1.U){
    alu_b := io.if_IDtoEX.imm
  }.otherwise{
    alu_b := io.if_RFtoEX.d_rs2
  }
  o_alu := 0.U
  // ALU
  switch(alu_func(3,0)) {
    is(OBJ_ALU_FUNC.ADD  ) { o_alu := alu_a + alu_b }
    is(OBJ_ALU_FUNC.SUB  ) { o_alu := alu_a - alu_b }
    is(OBJ_ALU_FUNC.XOR  ) { o_alu := alu_a ^ alu_b }
    is(OBJ_ALU_FUNC.OR   ) { o_alu := alu_a | alu_b }
    is(OBJ_ALU_FUNC.AND  ) { o_alu := alu_a & alu_b }
    is(OBJ_ALU_FUNC.SEL_A) { o_alu := alu_a         }
    is(OBJ_ALU_FUNC.SEL_B) { o_alu :=         alu_b }
  }

  io.if_EXtoWB.rd    := io.if_IDtoEX.rd
  io.if_EXtoWB.d_alu := o_alu
  io.if_EXtoWB.d_ld  := 0.U
  io.if_EXtoWB.valid :=  io.if_IDtoEX.valid
}
