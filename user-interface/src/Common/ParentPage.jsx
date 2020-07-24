import React, {Component} from 'react';
import PropTypes from 'prop-types';
import defaultTheme from '../Utils/Theme';
import {MuiThemeProvider} from 'material-ui/styles';
import Box from '@material-ui/core/Box';
import Paper from '@material-ui/core/Paper';

import Header from './Header';

const styles = {
    contentPaper: {
        height: "calc(100% - 50px)",
        width: 'calc(100% - 0px)',
        float: 'right',
        paddingTop: '40px',
        marginLeft: '0px',
        position: 'fixed',
        overflowY: 'auto'
    },
    footer: {
        padding: 5,
        position: "absolute",
        textAlign: "center",
        left: 0,
        bottom: 0,
        right: 0,
        backgroundColor: "#263238",
        height: '50px',
        marginTop: '25px',
        color: '#ffffff'
    },
};

class ParentPage extends Component {

    constructor(props) {
        super(props);
        this.state = {
            files: null
        };
    }

    componentDidMount() {

    }

    render() {
        return (
            <MuiThemeProvider muiTheme={this.props.theme}>
                <Header/>
                <Box flexGrow={1} id={"content-box"}>
                    <Paper style={styles.contentPaper} id={"data-box"} square={true}>
                        {this.props.data}
                    </Paper>
                </Box>
            </MuiThemeProvider>);
    }
}

ParentPage.propTypes = {
    theme: PropTypes.shape({}),
    data: PropTypes.element,
};

ParentPage.defaultProps = {
    data: <span/>,
    theme: defaultTheme,
};

export default (ParentPage);