# Documentation

Two versions of the documentation are kept in the 'master' branch:

* The `snapshot` version of the documentation applies to the SNAPSHOT versions of ktlint. Upon the publication of the next official release of ktlint, the `release-latest` version of the documentation is replaced with the `snapshot` version. See script `.announce` which is executed by the Github workflow `publish-release-build`.
* The `release-latest` version of the documentation applies to the last officially published version of ktlint. Upon the publication of the next official release of ktlint, this version of the documentation is replaced with the `snapshot` version. See script `.announce` which is executed by the Github workflow `publish-release-build`.

Whenever a fix is to be made to the documentation, it should be determined in which version(s) of the documentation is to be fixed. Documentation fixes which only apply to the `SNAPSHOT` version of ktlint may only be fixed in the `snapshot` version of the documentation.

Documentation changes related to the latest released version, and which can not wait to be published with the documentation of the next release, need to be fixed in both the `snapshot` and `release-latest` versions. Only fixing it in `release-latest` results in the fix being lost upon publication of the next official ktlint version. Small typo's can be fixed in both, but it is also okay to only fix them in the `snapshot` only.

IMPORTANT: Fixing the `snapshot` documentation is more important than fixing the `release-latest` documentation! Try to fix the `release-latest` only when the fix is important (e.g. misleading or confusing for users).

Docs can be viewed on the local machine in one of following ways:
* Run script `serve-docs-locally.sh` which starts a docker container running mkdocs
* Run command `mike serve` which requires that `python`, `pip`, `mike` and `mkdocs` have been installed (one time only) before

NOTE: Changes to the documentation have to be submitted as PR. When merging to `master` branch, the Github workflows `publish-snapshot-docs` and `publish-release-docs` take care of publishing the docs to Github Pages.

## Rebuilding Github Pages manually

Github Pages deploys the documentation from branch `gh-pages`. Each version of the documentation is stored in a separate directory of that branch. Each of those directories contains a html/css version of the generated documentation. Beside the directories for released versions, two special directories exist. The `latest` directory which is based on the `documentation/release-latest` directory on the `master` branch. The `dev-snapshot` directory is based on the `documentation/snapshot` directory on the `master` branch.

In case the `gh-pages` branch is corrupt, it can be recreated again. In order to execute commands below, `python`, `pip`, `mkdocs` and `mike` need to be installed on the local machine. The commands have to be executed from the designated directory. Note that after each command a Github Actions workflow is started (see https://github.com/ktlint/ktlint/actions). Wait with executing the next command until the Github Action workflow is completed. In between steps, the command `mike list` can be executed to see what versions have been published.

1) Clear all doc versions from gh-pages
    ```shell
    # Execute from directory `documentation/release-latest`
    mike delete --all --push
    ```
   After the Github Actions workflow is completed, the Github Pages will be empty, and displays a 404 error page.

2) Recreate the documentation for the last released version:
   Reset the active branch to the commit with description `Updated refs to latest (xx.yy.zz) release`.
   In directory `documentation/release-latest`:
    ```shell
    # Execute from directory `documentation/release-latest`
    mike deploy --push --update-aliases xx.yy.zz latest # Replace "xx.yy.zz" with the version number. Do not remove or alter tag "latest"
    ```
    After the Github Actions workflow is completed, the documentation is available at https://ktlint.github.io/ktlint/xx.yy.zz/ but is not yet available on https://ktlint.github.io/ktlint/ until the Github Action workflow for next command is completed:
    ```shell
    # Execute from directory `documentation/release-latest`
    ```
    mike set-default --push latest

3) Recreate the documentation for older releases:
   Reset the active branch to the commit with description `Updated refs to latest (aa.bb.cc) release`. Note that for releases prior to `0.49.0` additional changes are needed, dependent to which tag the branch is reset:
     - In case the `documentation` directory does not exist, then execute the command from the root directory of the project as the `mkdocs.yml` is residing in that directory.
     - Add file `documentation/release-latest/overrides/main.html` from branch `master` to the directory where the `mkdocs.yml` file resided
     - Add lines below to the `mkdocs.yml`:
       ```yaml
       extra:
       version:
       provider: mike
        
       theme:
         name: material
         custom_dir: overrides
       ```
   In directory `documentation/release-latest`:
    ```shell
    # Execute from directory `documentation/release-latest` or from root directory of project for versions prior to `0.49.0`
    mike deploy --push --update-aliases aa.bb.cc # Replace "aa.bb.cc" with the version number. Do not add tag "latest"!
    ```
   After the Github Actions workflow is completed, the documentation is available at https://ktlint.github.io/ktlint/aa.bb.cc/

4) Recreate the snapshot documentation
   In directory `documentation/snapshot`:
    ```shell
    # Execute from directory `documentation/snapshot`
    mike deploy --push --update-aliases dev-snapshot
    ```
   After the Github Actions workflow is completed, the documentation is available at https://ktlint.github.io/ktlint/dev-snapshot/.
