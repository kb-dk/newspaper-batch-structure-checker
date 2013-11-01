<?xml version='1.0' encoding='UTF-8'?>
<s:schema xmlns:s="http://purl.oclc.org/dsdl/schematron">

    <s:let name="batchID" value="/node/@name"/>

    <s:let name="batchNumber" value="substring-after(substring-before($batchID,'-'),'B')"/>

    <!-- Example: B400022028241-RT1/WORKSHIFT-ISO-TARGET -->
    <s:let name="workshiftISOTarget" value="'WORKSHIFT-ISO-TARGET'"/>


    <!-- Example: B400022028241-RT1/400022028241-14 -->
    <s:let name="filmIdPattern" value="concat('^',$batchNumber,'-[0-9]{2}$')"/>

    <s:let name="workshiftISOTargetPattern" value="concat('^','Target-[0-9]{6}-[0-9]{4}$')"/>

    <s:let name="datoUdgaveLbNummer" value="'^[12][0-9]{3}-(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])-[0-9]{2}$'"/>


    <s:pattern id="batchNodeChecker">

        <s:rule context="/node">
            <!--Check: batchNodeChecker: Form: B<batchid>-RT<roundtrip number>-->
            <s:assert test="matches(@name,'^B[0-9]{12}-RT[0-9]+$')">Invalid batch folder name
                <s:value-of select="@name"/>
                Expected form: B[batchid]-RT[roundtrip number]
            </s:assert>

            <!--Check: batchNodeChecker: Eksistens af workshift-iso-target BatchNodeChecker-->
            <s:assert test="node[@shortName = $workshiftISOTarget]">WORKSHIFT-ISO-TARGET not found in batch folder
            </s:assert>

        </s:rule>
        <s:rule context="/node/node[@shortName != $workshiftISOTarget]">
            <!-- Check: batchNodeChecker: Eksistens af en mappe per filmid
                 Check: batchNodeChecker: Ikke andre filer og mapper BatchNodeChecker-->
            <s:assert test="matches(@shortName,$filmIdPattern)">
                Unexpected folder '
                <s:value-of select="@name"/>
            </s:assert>
        </s:rule>

        <s:rule context="/node/attribute">
            <s:report test="true()">
                Unexpected file '<s:value-of select="@name"/>'
            </s:report>
        </s:rule>


    </s:pattern>


    <s:pattern id="workshiftIsoTargetChecker">
        <s:rule context="/node/node[@shortName=$workshiftISOTarget]">
            <!-- Check: Workshift-iso-target: Eksistens af Target-filer -->
            <s:assert test="count(node) != 0">
                No files in
                <s:value-of select="@name"/>
            </s:assert>
        </s:rule>

        <s:rule context="/node/node[@shortName=$workshiftISOTarget]/node">
            <!-- Check:  Workshift-iso-target: Names (nodes) in WORKSHIFT-ISO-TARGET must be of the right format: Target-[0-9]{6}-[0-9]{4} -->
            <s:assert test="matches(@shortName,$workshiftISOTargetPattern)">
                Unexpected file or folder found
                <s:value-of select="@name"/>
                Only files named like Target-[targetSerialisedNumber]-[billedID].jp2 or
                Target-[targetSerialisedNumber]-[billedID].mix.xml are allowed.
            </s:assert>
        </s:rule>

        <s:rule context="/node/node[@shortName=$workshiftISOTarget]/attribute">
            <!-- Check: Workshift-iso-target: Ikke andre filer og mapper -->
            <s:report test="true()">
                Unexpected file '<s:value-of select="@name"/>'
            </s:report>
        </s:rule>

    </s:pattern>


    <s:pattern id="workshiftImageChecker" is-a="scanChecker">
        <!--Target-filer:
                Check: Form: Target-[targetSerialisedNumber]-[billedID].(jp2|mix)
                Check: Én mix-fil pr. jp2-fil
                Check: 6-digit targetSerialisedNumber
                Check: 4-digit billedId-->
        <!-- Check: There must exist a file in each WORKSHIFT-ISO-TARGET/Target-[0-9]{6}-[0-9]{4} called Target-[0-9]{6}-[0-9]{4}.mix.xml -->
        <!-- Check: There must exist a jp2-node in each WORKSHIFT-ISO-TARGET/Target-[0-9]{6}-[0-9]{4} called Target-[0-9]{6}-[0-9]{4}.jp2 containing a contents attribute -->
        <s:param name="scan" value="/node/
          node[@shortName = $workshiftISOTarget]/
          node[matches(@shortName,$workshiftISOTargetPattern)]"/>
    </s:pattern>


    <s:pattern id="filmChecker">
        <!--Film-directories:
        TODO: Eksistens af edition-mapper (mindst en)
        -->

        <s:rule context="/node/node[@shortName != $workshiftISOTarget]">
            <!-- Check: Film-directories: Any folder in BATCH not called WORKSHIFT-ISO-TARGET must have name of format [0-9]{12}-[0-9]+ (a FILM folder) -->
            <s:assert test="matches(@shortName,$filmIdPattern)">
                unexpected folder '<s:value-of select="@name"/>'
            </s:assert>

            <!-- Check: Existence of film.xml -->
            <s:let name="filmNumber" value="@shortName"/>
            <s:assert test="count(attribute) = 1">
                Cannot find film metadata file in
                <s:value-of select="@name"/>
            </s:assert>

            <!--Check: XXX Edition-mappe: Eksistens af side-mapper-->
            <s:assert test="count(node[matches(@shortName,$datoUdgaveLbNummer)]) > 0">
                No editions in film
                <s:value-of select="@name"/>
            </s:assert>
        </s:rule>

        <s:rule context="/node/node[@shortName != $workshiftISOTarget]/node">
            <!--    Check: Potentiel eksistens af FILM-ISO-target
                    Check: Potentiel eksistens af UNMATCHED
            -->
            <s:assert test="matches(@shortName,concat('(^FILM-ISO-target$|^UNMATCHED$|',$datoUdgaveLbNummer,')'))">
                unexpected folder '<s:value-of select="@name"/>'
            </s:assert>

        </s:rule>

        <s:rule context="/node/node[@shortName != $workshiftISOTarget]/attribute">
            <!--Check: FilmChecker: Ikke andre filer og mapper-->
            <s:let name="filmNumber" value="../@shortName"/>
            <!--Check: film.xml-fil
                    Check: Form: [avisID]-[batchID]-[filmSuffix].film.xml FilmNodeChecker
                    Check: batchID er som i parent dir FilmNodeChecker
                    Check: filmSuffix er som i parent dir FilmNodeChecker
                    -->
            <s:assert test="matches(@shortName,concat('^','.*-',$filmNumber,'[.]film[.]xml$'))">
                Unexpected file '<s:value-of select="@name"/>'
            </s:assert>
        </s:rule>
    </s:pattern>

    <s:pattern id="unmatchedChecker" is-a="inFilmChecker">
        <!-- Check: Nodes in UNMATCHED must have format [avisID]-[filmID]-[0-9]{4}[A-Z]? where [avisID]-[filmID] is as found in the film metadata file for this film. -->
        <s:param name="inFilmPath"
                 value="/node/node[@shortName != $workshiftISOTarget]/node[@shortName = 'UNMATCHED']"/>
        <s:param name="postPattern" value="'-[0-9]{4}[A-Z]?'"/>
    </s:pattern>


    <s:pattern id="filmIsoTargetChecker" is-a="inFilmChecker">
        <!--
        Check: FILM-ISO-target: Præcist dette navn
        TODO: FILM-ISO-target: Eksistens af iso-filer? If FILM-ISO-target is not required to exist, do we demand contents when it does?
        Check: FILM-ISO-target: Ikke andre filer og mapper

        Check: FILM-ISO-target-filer: Form: [filmID]-[batchID]-[filmSuffix]-ISO-[1-9].(jp2|mix)
        Check: FILM-ISO-target-filer: Én mix-fil pr. jp2-fil
        Check: FILM-ISO-target-filer: filmID, [batchID], <filmSuffix> som i parent directory (filmID dog som film.xml i parent directory)
        -->
        <s:param name="inFilmPath"
                 value="/node/node[@shortName != $workshiftISOTarget]/node[@shortName = 'FILM-ISO-target']"/>
        <s:param name="postPattern" value="'-ISO-[1-9]'"/>
    </s:pattern>


    <s:pattern id="editionChecker">

        <s:rule context="/node/
           node[@shortName != $workshiftISOTarget]/
           node[ @shortName != 'FILM-ISO-target' and @shortName != 'UNMATCHED']">


            <!-- Check: Edition-mappe: Form: [date]-[udgaveLbNummer]
                    Check: Edition-mappe: [date] skal være iso8601
            -->
            <s:let name="filmID" value="../@shortName"/>
            <s:let name="editionID" value="@shortName"/>
            <s:assert test="matches($editionID,$datoUdgaveLbNummer)">
                Invalid Edition ID
                <s:value-of select="$editionID"/>
            </s:assert>

            <!--Check: Edition-mappe: Eksistens af side-mapper-->
            <s:assert test="count(node) > 0">
                No pages in edition
                <s:value-of select="@name"/>
            </s:assert>

            <!-- Check: test existence of edition.xml -->
            <s:let name="avisID"
                   value="replace(
                                substring-before(
                                    ../attribute[ends-with(@shortName,'.film.xml')]/@shortName,'.film.xml'),'[0-9]{12}-[0-9]{2}','')"/>
            <s:let name="editionXml" value="concat($avisID,$editionID,'.edition.xml')"/>
            <s:assert test="count(attribute[@shortName = $editionXml])=0">
                <s:value-of select="concat(@name,'/',$avisID,$editionID,'.edition.xml')"/>
                missing
            </s:assert>

        </s:rule>


        <s:rule context="/node/
                   node[@shortName != $workshiftISOTarget]/
                   node[ @shortName != 'FILM-ISO-target' and @shortName != 'UNMATCHED']/attribute">


            <s:let name="filmID" value="../../@shortName"/>
            <s:let name="editionID" value="../@shortName"/>

            <!-- Check: Edition-mappe: Eksistens af edition-fil
                 Check: Edition-mappe: Ingen andre filer og mapper
                 Check: Edition-mappe: Form: [avisID]-[date]-[udgaveLbNummer].edition.xml EditionNodeChecker
                 Check: Edition-mappe: [avisID], [date], [udgaveLbNummer] som i parent directory (avisID dog som film.xml i parent directory) EditionNodeChecker
            -->
            <s:let name="avisID"
                   value="replace(
                                substring-before(
                                    ../../attribute[ends-with(@shortName,'.film.xml')]/@shortName,'.film.xml'),'[0-9]{12}-[0-9]{2}','')"/>
            <s:assert test="@shortName = concat($avisID,$editionID,'.edition.xml')">
                Unexpected file
                <s:value-of select="@name"/>
            </s:assert>
        </s:rule>

    </s:pattern>


    <s:pattern id="editionPageChecker">

        <!-- Example: editionPageChecker: B400022028241-RT1/400022028241-14/1795-06-15-01/adresseavisen1759-1795-06-15-01-0002 -->
        <s:rule context="/node/node[@shortName != $workshiftISOTarget]/
                           node[ @shortName != 'FILM-ISO-target' and @shortName != 'UNMATCHED']/
                           node[ not(ends-with(@shortName,'-brik'))]">

            <s:let name="filmID" value="../../@shortName"/>
            <s:let name="editionID" value="../@shortName"/>

            <s:let name="avisID"
                   value="replace(
                                substring-before(
                                    ../../attribute[ends-with(@shortName,'.film.xml')]/@shortName,'.film.xml'),'[0-9]{12}-[0-9]{2}','')"/>

            <s:assert test="matches(@shortName,concat('^',$avisID,$editionID,'-[0-9]{4}[A-Z]?$'))">
                Invalid prefix for page '<s:value-of select="@name"/>'
            </s:assert>

            <!-- Check: editionPageChecker: Any node in BATCH/FILM/EDITION/ which is not a brik must contain a .alto.xml attribute -->
            <s:assert test="attribute/@shortName = concat(@shortName,'.alto.xml')">
                Alto file '<s:value-of select="concat(@name,'.alto.xml')"/>' missing
            </s:assert>
            <!-- TODO: Her ville vi skulle tage flag fra mf-pak om hvorvidt vi skulle forvente alto. Flag kunne indkodes i denne .sch fil before run-->

            <!-- Check: editionPageChecker: Any node in BATCH/FILM/EDITION/ which is not a brik must contain a .mods.xml attribute -->
            <s:assert test="attribute/@shortName = concat(@shortName,'.mods.xml')">
                Mods file '<s:value-of select="concat(@name,'.mods.xml')"/>' missing
            </s:assert>

            <!-- Check: editionPageChecker: Any node in BATCH/FILM/EDITION/ which is not a brik must contain a .mix.xml attribute -->
            <s:assert test="attribute/@shortName = concat(@shortName,'.mix.xml')">
                Mix file '<s:value-of select="concat(@name,'.mix.xml')"/>' missing
            </s:assert>

            <!-- Check: editionPageChecker: Any node in BATCH/FILM/EDITION/ which is not a brik must contain a .jp2 attribute -->
            <s:assert test="node/@shortName = concat(@shortName,'.jp2')">
                Jp2 file '<s:value-of select="concat(@name,'.jp2')"/>' missing
            </s:assert>
        </s:rule>

        <s:rule context="/node/node[@shortName != $workshiftISOTarget]/
                           node[ @name != 'FILM-ISO-target' and @name != 'UNMATCHED']/
                           node[ not(ends-with(@shortName,'-brik'))]/attribute">
            <!--Check: editionPageChecker: Any node in BATCH/FILM/EDITION/ can only contain mix, mods, alto and jp2 files -->
            <s:assert test="@shortName = concat(../@shortName,'.mix.xml') or @shortName = concat(../@shortName,'.mods.xml') or @shortName = concat(../@shortName,'.alto.xml')">
                Unexpected file '<s:value-of select="@name"/>' found
            </s:assert>
        </s:rule>


        <s:rule context="/node/node[@shortName != $workshiftISOTarget]/
                           node[ @shortName != 'FILM-ISO-target' and @shortName != 'UNMATCHED']/
                           node[ not(ends-with(@shortName,'-brik'))]/node">
            <!--Check: editionPageChecker: Any node in BATCH/FILM/EDITION/ can only contain mix, mods, alto and jp2 files -->
            <s:assert test="@shortName = concat(../@shortName,'.jp2')">
                Unexpected folder '<s:value-of select="@name"/>' found
            </s:assert>

            <s:assert test="attribute[@shortName='contents']">
                Contents not found for jp2file '<s:value-of select="@name"/>'
            </s:assert>
        </s:rule>
    </s:pattern>


    <s:pattern id="unmatchedPageChecker">

        <s:rule context="/node/
                        node[@shortName != $workshiftISOTarget]/
                        node[@shortName = 'UNMATCHED']/
                        node">
            <s:let name="editionID" value="../@shortName"/>

            <!-- Check: unmatchedPageChecker: Any node in BATCH/FILM/UNMATCHED/ must contain a .mix.xml attribute -->
            <s:assert test="attribute/@shortName = concat(@shortName,'.mix.xml')">
                Mix file '<s:value-of select="concat(@name,'.mix.xml')"/>' missing
            </s:assert>

            <!-- Check: unmatchedPageChecker: Any node in BATCH/FILM/UNMATCHED/ must contain a .jp2 attribute -->
            <s:assert test="node/@shortName = concat(@shortName,'.jp2')">
                Jp2 file '<s:value-of select="concat(@name,'.jp2')"/>' missing
            </s:assert>
        </s:rule>

        <s:rule context="/node/
                        node[@shortName != $workshiftISOTarget]/
                        node[@shortName = 'UNMATCHED']/
                        node/attribute">
            <!--Check: unmatchedPageChecker: Any node in BATCH/FILM/UNMATCHED/ can only contain mix, mods, alto and jp2 files -->
            <s:assert test="@name = concat(../@name,'.mix.xml') or @name = concat(../@name,'.mods.xml') or @name = concat(../@name,'.alto.xml')">
                Unexpected file '<s:value-of select="@name"/>' found
            </s:assert>
        </s:rule>


        <s:rule context="/node/
                        node[@shortName != $workshiftISOTarget]/
                        node[@shortName = '/UNMATCHED']/
                        node/node">
            <!--Check: unmatchedPageChecker: Any node in BATCH/FILM/UNMATCHED/ can only contain mix, mods, alto and jp2 files -->
            <s:assert test="@name = concat(../@name,'.jp2')">
                Unexpected folder '<s:value-of select="@name"/>' found
            </s:assert>

            <s:assert test="attribute[@name=concat(../@name,'/contents')]">
                Contents not found for jp2file '<s:value-of select="@name"/>'
            </s:assert>
        </s:rule>
    </s:pattern>

    <s:pattern id="brikChecker" is-a="scanChecker">
        <!--Check: Edition-mappe: Potentiel eksistens af brik-mapper-->
        <!-- Check: brikChecker: Any node in BATCH/FILM/EDITION/ which is a brik must contain a .mix.xml attribute -->
        <!-- Check: brikChecker: Any node in BATCH/FILM/EDITION/ which is a brik must contain a .jp2 attribute -->

        <s:param name="scan"
                 value="/node/node[@shortName != $workshiftISOTarget]/
                                            node[ @shortName != 'FILM-ISO-target' and @shortName != 'UNMATCHED']/
                                            node[ ends-with(@shortName,'-brik')]"/>
    </s:pattern>


    <s:pattern id="filmIsoTargetChecker" is-a="scanChecker">
        <!-- Check: brikChecker: Any node in BATCH/FILM/FILM-ISO-target/ must contain a .mix.xml attribute -->
        <!-- Check: brikChecker: Any node in BATCH/FILM/FILM-ISO-target/ must contain a .jp2 attribute -->

        <s:param name="scan"
                 value="/node/node[@shortName != $workshiftISOTarget]/
                                                             node[ @shortName = 'FILM-ISO-target']/
                                                             node"/>
    </s:pattern>


    <s:pattern id="checksumExistenceChecker">
        <s:rule context="attribute">
            <!-- Check: checksumExistenceChecker: Every file must have a checksum -->
            <s:report test="@checksum = 'null'">
                Checksum not found for
                <s:value-of select="@name"/>
            </s:report>
        </s:rule>
    </s:pattern>


    <!-- This abstract pattern checks a "scan" i.e. a jp2 node, its contents attribute, and corresponding mix file -->
    <s:pattern abstract="true" id="scanChecker">
        <s:rule context="$scan">
            <s:assert test="attribute/@name = concat(@name,'.mix.xml')">
                Mix not found in
                <s:value-of select="@name"/>
            </s:assert>

            <s:assert test="node/@name = concat(@name,'.jp2')">
                jp2 not found in
                <s:value-of select="@name"/>
            </s:assert>
        </s:rule>

        <s:rule context="$scan/attribute">
            <s:assert test="@name = concat(../@name,'.mix.xml')">
                Unexpected file '<s:value-of select="@name"/>' found
            </s:assert>
        </s:rule>

        <s:rule context="$scan/node">
            <s:assert test="@name = concat(../@name,'.jp2')">
                Unexpected folder '<s:value-of select="@name"/>' found
            </s:assert>

            <s:assert test="attribute[@name=concat(../@name,'/contents')]">
                Contents not found for jp2file '<s:value-of select="@name"/>'
            </s:assert>
        </s:rule>
    </s:pattern>

    <!-- This abstract pattern is used to check that no unexpected files are found in UNMATCHED or FILM-ISO-target -->
    <s:pattern abstract="true" id="inFilmChecker">
        <s:rule context="$inFilmPath/node">
            <s:let name="filmName"
                   value="
                   substring-before(
                   ../../attribute[ends-with(@shortName,'.film.xml')]/@shortName,'.film.xml')"/>
            <s:assert test="matches(@name, concat($filmName,$postPattern))">
                Unexpected file
                <s:value-of select="@name"/>
            </s:assert>
        </s:rule>
    </s:pattern>

</s:schema>
