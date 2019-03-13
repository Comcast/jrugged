Contribution Guidelines
=======================

We love to see contributions to the project and have tried to make it
easy to do so. If you would like to contribute code to this project
you can do so through GitHub by forking the repository and sending a
pull request.

Before Comcast merges your code into the project you must sign the
[Comcast Contributor License Agreement (CLA)](http://comcast.github.io/sirius/ComcastContributorLicenseAgreement_03-07-14.pdf).

If you haven’t previously signed a Comcast CLA, we can e-mail you a PDF
that you can sign and scan back to us.  Please send us an e-mail or create
a new GitHub issue to request a PDF version of the CLA.

For more details about contributing to github projects see
http://gun.io/blog/how-to-github-fork-branch-and-pull-request/

Documentation
-------------

If you contribute anything that changes the behaviour of the
application, document it in the README or wiki! This includes new
features, additional variants of behaviour and breaking changes.

Make a note of breaking changes in the pull request because they will
need to go into the release notes.

Testing
-------

This project primarily utilizes JUnit based testing using
Mockito for making those things that can not be easily
represented or consumed directly when testing a given class.
We love when people provide ANY tests for the work they are submitting 
but for sure we give preference to JUnit and Mockito.

Pull Requests
-------------

* should be from a forked project with an appropriate branch name
* should be narrowly focused with no more than 3 or 4 logical commits
* when possible, address no more than one issue
* should be reviewable in the GitHub code review tool
* should be linked to any issues it relates to (ie issue number after
(#) in commit messages or pull request message)

Expect a thorough review process for any pull requests that add functionality
or change the behavior of the application. We encourage you to sketch your
approach in writing on a relevant issue (or creating such an issue if needed)
before starting to code, in order to save time and frustration all around. 

Commit messages
---------------

Please follow the advice of the
[Phonegap team](https://github.com/phonegap/phonegap/wiki/Git-Commit-Message-Format)
when crafting commit messages. The advice basically comes down to:

* First line should be maximum 50 characters long
* It should summarise the change and use imperative present tense
* The rest of the commit message should come after a blank line
* We encourage you to use Markdown syntax in the rest of the commit
message
* Preferably keep to an 72 character limit on lines in the rest of the
message.

If a commit is related to a particular issue, put the issue number
after a hash (#) somewhere in the detail. You can put the issue number
in the first line summary, but only if you can also fit in a useful
summary of what was changed in the commit.

Here's an example git message:

```

Allow for an CircuitBreaker status change callback

When a circuit breaker changes it state we would like to 
allow individuals to be notified via callback that the state
of that break did change and what it changed from and what
it changed to.

```

Formatting
----------

The rules are simple: use the same formatting as the rest of the code.
The following is a list of the styles we are strict about:

* 2 space indent, no tabs
* a space between if/elseif/catch/etc. keywords and the parenthesis
* 120 character line maximum

