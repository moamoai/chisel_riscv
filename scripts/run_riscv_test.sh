#!/bin/bash -e

mkdir -p logs

# LOG_DIR="../riscv_testpattern/logs/"
# PAT_DIR="../01_comp/riscv-tests/isa/"
# ls -1 ${PAT_DIR} | grep -e "rv32ui-p" | grep -v "dump" > test_list_all

grep -v "#" test_list_all > test_list

echo "" > result.log
for test in `cat test_list`
do
  echo "# ${test}"
  rm -f run_spike.log
  rm -f pattern.dump
  ln -s ${LOG_DIR}/run_${test}.log ./run_spike.log
  ln -s ${PAT_DIR}/${test}.dump    ./pattern.dump
  python tools/gen_expect.py > gen_expect.log
  python tools/gen_inst.py   > gen_inst.log 
  set +e
  riscv64-unknown-elf-objdump -s -j .data  ${PAT_DIR}/${test} > data.dump
  set -e
  python tools/gen_data.py
 
  LOG_FILE=logs/run_${test}.log
  make riscv-test | tee ${LOG_FILE}
  RESULT=`grep -e "OK" -e "NG" -e "ERROR" ${LOG_FILE}`
  TIME=`grep -e "Total time" ${LOG_FILE}`
  echo "${LOG_FILE}: ${RESULT} ${TIME}"  >> result.log
done
