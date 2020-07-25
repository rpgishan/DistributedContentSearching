import React, {Component} from 'react';
import Parent from '../Common/ParentPage';
import Box from '@material-ui/core/Box';
import Button from '@material-ui/core/Button';
import 'bootstrap/dist/css/bootstrap.css';
import BootStrapAPI from '../Apis/BootStrapAPI';
import NodeAPI from '../Apis/NodeAPI';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';

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
    tableHead: {
        textAlign: 'center',
        fontWeight: 'bold'
    },
    tableData: {
        textAlign: 'center'
    },
    resultArea: {
        marginTop: '5%'
    },
    button1: {
        border: '1px%'
    }
};

class NodeDistributionPage extends Component {

    constructor(props) {
        super(props);
        this.state = {
            nodes: [],
            connectedNodes: [],
            error: null
        };
        this.retrieveAllNodes = this.retrieveAllNodes.bind(this);
        this.retrieveConnectedNodes = this.retrieveConnectedNodes.bind(this);
    }

    handleErrorResponses(error) {
        this.setState({error: error})
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

    retrieveConnectedNodes(node) {
        new NodeAPI().retrieveConnectedNodes(node).then((response) => {
            console.log(response.data.nodes);
            if (response.data.nodes != null) {
                var nodeListString = "#  ";
                response.data.nodes.map(( listValue, index ) => {
                    nodeListString = nodeListString + listValue.host + ":" + listValue.port + "  #  ";
                })
                this.state.connectedNodes.push(nodeListString);
                console.log(this.state.connectedNodes);
            }
            return response.data.nodes;
        }).catch((error) => {
            this.handleErrorResponses(error);
        });
    }

    renderNodeDistributionContent() {
        return (
            <Box style={styles.container}>
                <Box style={styles.headingArea}>
                    <h2 style={styles.heading}><b>NODE DISTRIBUTION</b></h2>
                </Box>
                <Button variant="outlined" onClick={() => {
                    this.retrieveAllNodes();
                    this.state.nodes.map((nodeInstance, index) => {
                        this.retrieveConnectedNodes(this.state.nodes[index]);
                    })
                }}>Refresh Connected Nodes</Button>
                <Box style={styles.resultArea}>
                    <Table aria-label="simple table">
                        <TableHead>
                            <TableRow>
                                <TableCell style={styles.tableHead}>#</TableCell>
                                <TableCell style={styles.tableHead}>Host</TableCell>
                                <TableCell style={styles.tableHead}>Port</TableCell>
                                <TableCell style={styles.tableHead}>Connected Nodes</TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {this.state.nodes.map(( listValue, index ) => {
                                return (
                                    <TableRow key={index}>
                                        <TableCell style={styles.tableData}>{index}</TableCell>
                                        <TableCell style={styles.tableData}>{listValue.host}</TableCell>
                                        <TableCell style={styles.tableData}>{listValue.port}</TableCell>
                                        <TableCell style={styles.tableData}>{this.getNodeString(index)}</TableCell>
                                    </TableRow>
                                );
                            })}
                        </TableBody>
                    </Table>
                </Box>
            </Box>
        );
    }

    getNodeString(index) {
        console.log(this.state.connectedNodes);
        return (this.state.connectedNodes[index])
    }
    render() {
        return (<Parent data={this.renderNodeDistributionContent()} error={this.state.error}/>);
    }
}

export default (NodeDistributionPage);
