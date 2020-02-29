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
    is(OBJ_ALU_FUNC.ADD  ) { o_alu :=  alu_a +  alu_b     }
    is(OBJ_ALU_FUNC.SLL  ) { o_alu :=  alu_a << alu_b(4,0)}
    is(OBJ_ALU_FUNC.SLT  ) { o_alu := (alu_a <  alu_b)    } // ok?
    is(OBJ_ALU_FUNC.SLTU ) { o_alu := (alu_a <  alu_b)    } // tmp
    is(OBJ_ALU_FUNC.SRL  ) { o_alu :=  alu_a >> alu_b(4,0)}

    is(OBJ_ALU_FUNC.XOR  ) { o_alu := alu_a ^ alu_b }
    is(OBJ_ALU_FUNC.OR   ) { o_alu := alu_a | alu_b }
    is(OBJ_ALU_FUNC.AND  ) { o_alu := alu_a & alu_b }
    is(OBJ_ALU_FUNC.SUB  ) { o_alu := alu_a - alu_b }
    is(OBJ_ALU_FUNC.SEL_A) { o_alu := alu_a         }
    is(OBJ_ALU_FUNC.SEL_B) { o_alu :=         alu_b }
  }
  // Load Store

  // Memory
  val addr = Wire(UInt(16.W))
  val i_mem = Module(new Memory)
  addr           := (io.if_RFtoEX.d_rs1 + io.if_IDtoEX.imm)(15,0)
  i_mem.io.wdata := io.if_RFtoEX.d_rs2
  i_mem.io.we    := io.if_IDtoEX.store_valid
  i_mem.io.addr  := addr
  
  val wb_valid = Wire(UInt(1.W))
  val wb_data  = Wire(UInt(32.W))
  wb_data := 0.U
  wb_valid := io.if_IDtoEX.alu_valid  |
              io.if_IDtoEX.load_valid
  when(io.if_IDtoEX.alu_valid === 1.U){
    wb_data := o_alu
  }.elsewhen(io.if_IDtoEX.load_valid === 1.U){
    wb_data := i_mem.io.rdata
  }

  io.if_EXtoWB.rd       := io.if_IDtoEX.rd
  io.if_EXtoWB.wbdata   := wb_data
  io.if_EXtoWB.wbvalid := wb_valid
  io.if_EXtoWB.valid    := wb_valid | io.if_IDtoEX.store_valid
}
