import React, {Component} from 'react';
import PropTypes from 'prop-types';
import defaultTheme from '../Utils/Theme';
import {MuiThemeProvider} from 'material-ui/styles';
import Box from '@material-ui/core/Box';
import Paper from '@material-ui/core/Paper';
import Dialog from '@material-ui/core/Dialog';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import DialogContentText from '@material-ui/core/DialogContentText';
import DialogTitle from '@material-ui/core/DialogTitle';
import Button from '@material-ui/core/Button';

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
            responseErrorDialogOpen: false,
            serverErrorMsg: {}
        };
        this.handlerResponseErrorDialogClose = this.handlerResponseErrorDialogClose.bind(this);
    }

    handleError(error) {
        var errorMessage = 'Something went wrong. Please try again later.';

        if (error.response){
            if (error.response.data.error) {
                errorMessage = error.response.data.error;
            }
        } else if (error.request) {
            errorMessage = "Could not establish connection to bootstrap server."
        }

        this.setState({
            responseErrorDialogOpen: true,
            serverErrorMsg: errorMessage
        });
    }

    handlerResponseErrorDialogClose() {
        this.setState({
            responseErrorDialogOpen: false
        });
    }

    componentDidUpdate(prevProps, prevState, snapshot) {
        if (this.props.error !== prevProps.error) {
            if (this.props.error != null) {
                this.handleError(this.props.error);
            }
        }
    }

    render() {
        return (
            <MuiThemeProvider muiTheme={this.props.theme}>
                <Box>
                    <Header/>
                    <Box flexGrow={1} id={"content-box"}>
                        <Paper style={styles.contentPaper} id={"data-box"} square={true}>
                            {this.props.data}
                        </Paper>
                    </Box>
                    <Dialog open={this.state.responseErrorDialogOpen} onClose={this.handlerResponseErrorDialogClose}
                            aria-labelledby="alert-dialog-title" aria-describedby="alert-dialog-description">
                        <DialogTitle id="alert-dialog-title">{"Error"}</DialogTitle>
                        <DialogContent dividers>
                            <DialogContentText id="alert-dialog-description">
                                {this.state.serverErrorMsg}
                            </DialogContentText>
                        </DialogContent>
                        <DialogActions>
                            <Button onClick={this.handlerResponseErrorDialogClose} color="primary" autoFocus>
                                OK
                            </Button>
                        </DialogActions>
                    </Dialog>
                </Box>
            </MuiThemeProvider>
        );
    }
}

ParentPage.propTypes = {
    theme: PropTypes.shape({}),
    data: PropTypes.element,
    error: PropTypes.object,
};

ParentPage.defaultProps = {
    data: <span/>,
    theme: defaultTheme,
    error: null,
};

export default (ParentPage);