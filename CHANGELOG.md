1.2.3
* Updated to newspaper-batch-event-framework 1.4.4.
* Parameter autonomous.component.maxResults is now supported.

1.2.2
* Remove the last use of System.out.println()

1.2.1
* Update to newpaper-batch-event-framework 1.4.2, to make the component quiet on stderr

1.2
- Update to newspaper-batch-event-framework 1.4
- Update to mfpak-integration 1.3
- Add support for fuzzy dates

1.1
- Batch event framework to version 1.1 as 1.0 used invalid DOMS dependencies
- Rework of the config files to remove redundancy, among other things
- Check for option B1/B2/B9 existence of ALTO files
- Bugfix: Allow more than one film per batch
- Use newest batch event framework 1.2

1.0
- MFPak database related checks
- File names are now referred correctly in the QA report
- All checks refer to specifications
- Batch structure xml stored in DOMS or in file
- Film Id can be any length of numbers
- All sequence numbers are now checked

0.2
Simple file structure checks converted to Schematron.
All checks done, except:
  MFPak database related checks
  Numbering sequence checks (page image numbering checks are implemented though).

0.1
Initial release

