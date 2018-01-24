module.exports = function(grunt) {

    // Project configuration.
    grunt.initConfig({
        mochacov: {
            html:{
                options: {
                    slow: 1250,
                    timeout: 3000,
                    reporter: 'html-cov',
                    output: 'coverage/index.html',
                    instrument: true
                },
                src: ['test/**/*.js']
            },
            lcov:{
                options: {
                    slow: 1250,
                    timeout: 3000,
                    reporter: 'mocha-lcov-reporter',
                    output: 'coverage/report.lcov',
                    instrument: true
                },
                src: ['test/**/*.js']
            }
        },
        mochaTest: {
            options: {
                slow: 1250,
                timeout: 3000,
                reporter: 'spec',
                ignoreLeaks: false
            },
            src: ['test/**/*.js']
        },
        jshint: {
            options: {
                jshintrc: true
            },
            src: ['*.js','lib/**/*.js','test/**/*.js','util/**/*.js']
        }
    });

    // Load grunt plugins for modules
    grunt.loadNpmTasks('grunt-mocha-cov');
    grunt.loadNpmTasks('grunt-mocha-test');
    grunt.loadNpmTasks('grunt-contrib-jshint');

    // register tasks
    grunt.registerTask('coverage', ['mochacov:html','mochacov:lcov']);
    grunt.registerTask('default', ['jshint', 'mochaTest']);
    grunt.registerTask('lint', ['jshint']);
    grunt.registerTask('test', ['mochaTest']);

};
