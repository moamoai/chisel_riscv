package simple

import chisel3._
import chisel3.util._

import chisel3.util.experimental.loadMemoryFromFile

class EX extends Module {
  val io = IO(new Bundle {
    var if_IDtoEX = Flipped(new IF_IDtoEX)
    var if_RFtoEX = Flipped(new IF_RFtoEX)
    var if_EXtoWB = new IF_EXtoWB

    // Back door for test
    val if_mem_bd = new IF_MEM_BD
  })

  var alu_func  = io.if_IDtoEX.alu_func
  var ldst_func = io.if_IDtoEX.ldst_func
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
  // val i_mem = Module(new Memory)
  val i_mem = Module(new Memory_BD)
  val wdata    = Wire(UInt(32.W))
  wdata := 0.U
  val rdata    = Wire(UInt(32.W))
  var d_rs2    = io.if_RFtoEX.d_rs2
  var d_rs2_h  = d_rs2(15,0)
  var d_rs2_b  = d_rs2( 7,0)
  addr           := (io.if_RFtoEX.d_rs1 + io.if_IDtoEX.imm)(15,0)
  when(io.if_IDtoEX.store_valid === 1.U){ // Store
    when(ldst_func===0.U){         // SB
      when(addr(1,0) === 0x0.U){
        wdata := Cat(rdata(31,8), d_rs2_b)
      }.elsewhen(addr(1,0) === 0x1.U){
        wdata := Cat(rdata(31,16), d_rs2_b, rdata(7,0))
      }.elsewhen(addr(1,0) === 0x2.U){
        wdata := Cat(rdata(31,24), d_rs2_b, rdata(15,0))
      }.elsewhen(addr(1,0) === 0x3.U){
        wdata := Cat(d_rs2_b, rdata(23,0))
      }
    }.elsewhen(ldst_func === 1.U){ // SH
      when(addr(1,0) === 0x0.U){
        wdata := Cat(rdata(31,16), d_rs2_h)
      }.elsewhen(addr(1,0) ===0x2.U){
        wdata := Cat(d_rs2_h, rdata(15, 0))
      }
    }.elsewhen(ldst_func === 2.U){ // SW
        wdata := d_rs2
    }
  }
  i_mem.io.wdata := wdata
  i_mem.io.we    := io.if_IDtoEX.store_valid
  i_mem.io.addr  := addr & 0xFFFC.U // tmp?
  i_mem.io.if_mem_bd <> io.if_mem_bd

  val rdata_b  = Wire(UInt(8.W ))
  val rdata_h  = Wire(UInt(16.W))
  rdata_b := 0.U
  rdata_h := 0.U
  rdata := i_mem.io.rdata
  
  val wb_valid = Wire(UInt(1.W))
  val wb_data  = Wire(UInt(32.W))
  wb_data := 0.U
  wb_valid := io.if_IDtoEX.alu_valid  |
              io.if_IDtoEX.load_valid
  when(io.if_IDtoEX.alu_valid === 1.U){
    wb_data := o_alu
  }.elsewhen(io.if_IDtoEX.load_valid === 1.U){
    when(addr(1,0) === 0x0.U){
      rdata_b := rdata(7,0)
    }.elsewhen(addr(1,0) === 0x1.U){
      rdata_b := rdata(15,8)
    }.elsewhen(addr(1,0) === 0x2.U){
      rdata_b := rdata(23,16)
    }.elsewhen(addr(1,0) === 0x3.U){
      rdata_b := rdata(31,24)
    }
    when(addr(1,0) === 0x0.U){
      rdata_h := rdata(15,0)
    }.elsewhen(addr(1,0) ===0x2.U){
      rdata_h := rdata(31,16)
    }

    when(ldst_func===0.U){       // LB
      wb_data := Cat(Fill(24, rdata_b(7)) , rdata_b( 7,0))
    }.elsewhen(ldst_func===4.U){ // LBU
      wb_data := rdata_b
    }.elsewhen(ldst_func === 1.U){ // LH
      wb_data := Cat(Fill(16, rdata_h(15)), rdata_h(15,0))
    }.elsewhen(ldst_func === 5.U){ // LHU
      wb_data := rdata_h
    }.elsewhen(ldst_func === 2.U){ // LW
      wb_data := rdata
    }
//    }.elsewhen(ldst_func === 4.U){ // LBU
//      wb_data := rdata(7,0)
//    }.elsewhen(ldst_func === 5.U){ // LHU
//      wb_data := rdata(15,0)
//    }
  }

  io.if_EXtoWB.rd       := io.if_IDtoEX.rd
  io.if_EXtoWB.wbdata   := wb_data
  io.if_EXtoWB.wbvalid := wb_valid
  io.if_EXtoWB.valid    := wb_valid | io.if_IDtoEX.store_valid
}
