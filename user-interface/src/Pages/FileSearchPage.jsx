import React, {Component} from 'react';
import Parent from '../Common/ParentPage';
import Box from '@material-ui/core/Box';
import Button from '@material-ui/core/Button';
import Form from 'react-bootstrap/Form'
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import 'bootstrap/dist/css/bootstrap.css';
import BootStrapAPI from '../Apis/BootStrapAPI';
import NodeAPI from '../Apis/NodeAPI';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import {Link} from 'react-router-dom';

const styles = {
    container: {
        width: '60%',
        marginLeft: '20%',
        marginRight: '20%',
    },
    headingArea: {
        float: 'center',
        width: '60%',
        marginLeft: '20%',
        marginRight: '20%',
        marginBottom: '10%'
    },
    heading: {
        textAlign: 'center'
    },
    resultArea: {
        marginTop: '10%'
    }
};

class FileSearchPage extends Component {

    constructor(props) {
        super(props);
        this.state = {
            fileName: '',
            fileList: null,
            nodes: [],
            error: null,
            searchResult: null,
            fileDownloadLink: ''
        };

        this.handleSearch = this.handleSearch.bind(this);
        this.handleUserInput = this.handleUserInput.bind(this);
    }

    handleErrorResponses(error) {
        this.setState({error: error})
    }


    handleSearch(event) {
        event.preventDefault();
        const nodes = this.state.nodes;
        var nodeCount = nodes.length;
        // Pick random node from list
        if (nodeCount > 0) {
            var randomNodeIndex = Math.floor(Math.random() * ((nodeCount - 1) - 0 + 1)) + 0;
            var node = nodes[randomNodeIndex];
            // Sending the search request to the node
            new NodeAPI().searchFile(node, this.state.fileName).then((response) => {
                var result = response.data;
                var downloadLink = `http://${result.host}:${result.port+100}/retrieveFile/${result.fileName}.txt`
                this.setState({searchResult: response.data, fileDownloadLink:downloadLink})
            }).catch((error) => {
                this.handleErrorResponses(error);
            });
        } else {
            var error = {response: {data: {error: "No nodes available in the network."}}};
            this.handleErrorResponses(error);
        }

        // this.state.nodes.map((node) => {
        //     window.alert(node.host)
        // });

        // TODO: List the returned file list in a table
    }

    handleUserInput(event) {
        const target = event.target;
        const name = target.name;
        const value = target.value;
        this.setState({[name]: value});
    }

    componentDidMount() {
        this.retrieveAllNodes();
    }

    retrieveAllNodes() {
        new BootStrapAPI().getAllNodes().then((response) => {
            console.log(response.data.nodes);
            const nodes = response.data.nodes;
            this.setState({nodes: nodes});
        }).catch((error) => {
            this.handleErrorResponses(error);
        });
    }

    isSubmissionInValid() {
        const {fileName} = this.state;
        if (fileName === '') {
            return true;
        }
        return false;
    }

    // handleFileDownload(host, port, file) {
    //     var node = {host:host, port:port}
    //     new NodeAPI().retrieveFile(node, file).response()
    // }

    renderFileSearchContent() {
        return (
            <Box style={styles.container}>
                <Box style={styles.headingArea}>
                    <h2 style={styles.heading}><b>DISTRIBUTED FILE SEARCH</b></h2>
                </Box>
                <Form onSubmit={this.handleSearch}>
                    <Row>
                        <Col xs={11}>
                            <Form.Control placeholder="File name goes here.." name="fileName"
                                          onChange={this.handleUserInput}/>
                        </Col>
                        <Col xs={1}>
                            <Button variant="outlined" type="submit"
                                    disabled={this.isSubmissionInValid()}>Search</Button>
                        </Col>
                    </Row>
                </Form>

                {
                    this.state.searchResult != null ?
                        <Box style={styles.resultArea}>
                            <Table aria-label="simple table">
                                <TableHead>
                                    <TableRow>
                                        <TableCell>File</TableCell>
                                        <TableCell align="right">Host</TableCell>
                                        <TableCell align="right">Port</TableCell>
                                    </TableRow>
                                </TableHead>
                                <TableBody>
                                    <TableRow key={this.state.searchResult.fileName}>
                                        <TableCell align="right" component={Link}
                                                   to={this.state.fileDownloadLink}>{this.state.searchResult.fileName}</TableCell>
                                        <TableCell align="right">{this.state.searchResult.host}</TableCell>
                                        <TableCell align="right">{this.state.searchResult.port}</TableCell>
                                    </TableRow>
                                </TableBody>
                            </Table>
                        </Box>
                        :
                        null
                }

            </Box>);
    }

    render() {
        return (<Parent data={this.renderFileSearchContent()} error={this.state.error}/>);
    }
}

export default (FileSearchPage);