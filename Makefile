#
# Building Chisel examples without too much sbt/scala/... stuff
#
# sbt looks for default into a folder ./project and . for build.sdt and Build.scala
# sbt creates per default a ./target folder

SBT = sbt

# Generate Verilog code


riscv:
	$(SBT) "runMain simple.RiscVMain"

riscv-test:
	$(SBT) "test:runMain simple.RiscVTester --backend-name verilator"

GTKWAVE = /Applications/gtkwave.app/Contents/Resources/bin/gtkwave

view:
	$(GTKWAVE) ./test_run_dir/simple.RiscVTester1912282513/RiscV.gtkw
	# $(GTKWAVE) ./test_run_dir/simple.RiscVTester1912282511/RiscV.gtkw

# clean everything (including IntelliJ project settings)

clean:
	git clean -fd
