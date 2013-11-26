0.1
Initial release

0.2
Simple file structure checks converted to Schematron.
All checks done, except:
  MFPak database related checks
  Numbering sequence checks (page image numbering checks are implemented though).

1.0
- MFPak database related checks
- File names are now referred correctly in the QA report
- All checks refer to specifications
- Batch structure xml stored in DOMS or in file
- Film Id can be any length of numbers
- All sequence numbers are now checked

1.1
- Batch event framework to version 1.1 as 1.0 used invalid DOMS dependencies
- Rework of the config files to remove redundancy, among other things
